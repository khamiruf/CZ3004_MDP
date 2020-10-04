import json
import time
from multiprocessing import Process, Queue

from src.detector.SymbolDetector import SymbolDetector

from src.communicator.Android import Android
from src.communicator.Arduino import Arduino
from src.communicator.PC import PC
from src.communicator.utils import format_for, pcMsgParser
from src.Logger import Logger
from src.protocols import *

log = Logger()

'''
New structure for multiprocessing to have only 2 queues
Image Recognition process to run in main program
'''

class MultiProcess:
    def __init__(self, verbose):
        log.info('Initializing Multiprocessing Communication')
        self.verbose = verbose
        
        # the current action / status of the robot
        self.status = Status.STATUS_IDLE # default status

        # entities
        self.android = Android()
        self.arduino = Arduino()
        self.pc = PC()
        self.detector = SymbolDetector()  ##IMAGE REC

        # queues
        self.msg_queue = Queue()
        self.img_queue = Queue()    ##IMAGE REC

        # processes
        self.read_android_process = Process(target=self.read_android, args=(self.msg_queue,))
        self.read_arduino_process = Process(target=self.read_arduino, args=(self.msg_queue,))
        self.read_pc_process = Process(target=self.read_pc, args=(self.msg_queue, self.img_queue,))
        
        self.write_process = Process(target=self.write_target, args=(self.msg_queue,))

        
    def start(self):
        try:
            # connect all entities
            self.android.connect()
            self.arduino.connect()
            self.pc.connect()

            # start all processes
            self.read_android_process.start()
            self.read_arduino_process.start()
            self.read_pc_process.start()

            self.write_process.start()
            
            # log.info('Launching Symbol Detector')
            # self.detector()  #quite useless - only useless cause we havent write the class yet

            log.info('Multiprocess Communication Session Started')

            img_count = 0  ##IMAGE REC
            while True:
                if not self.img_queue.empty():
                    log.info('img_queue not empty')
                    msg = self.img_queue.get_nowait()
                    msg = json.loads(msg) # returns a dict
                    cmd = msg['payload']['command']
                    if cmd == 'TP':
                        log.info('Calling img detector')
                        fn = msg['payload']['coord']
                        self.detector.main(fn=fn)
                        log.info("Successfully took a picture")
                        log.info('Detecting for Symbols')
                        frame = self.detector.get_frame(fn)
                        symbol_match = self.detector.detect(frame)
                        img_count += 1
                        # if symbol_match is not None:
                        #     log.info('Symbol Match ID: ' + str(symbol_match))
                        #     self.pc.write('TC|' + str(symbol_match))
                        # else:
                        #     log.info('No Symbols Detected')
                        #     self.pc.write('TC|0')
        except KeyboardInterrupt:
            raise
        except Exception as error:
            raise error

    def end(self):
        log.info('Multiprocess Communication Session Ended')


    def read_android(self, msg_queue):
        while True:
            try:
                msg = self.android.read()
                if msg is not None:
                    if self.verbose:
                        log.info('Read Android: ' + str(msg))
                    # for remote commands
                    if msg == 'F01':
                        # msg_queue.put_nowait(format_for('AND', Status.STATUS_MOVING_FORWARD))
                        msg_queue.put_nowait(format_for('ARD', msg))
                    elif msg == 'L0':
                        # msg_queue.put_nowait(format_for('AND', Status.STATUS_TURNING_LEFT))
                        msg_queue.put_nowait(format_for('ARD', msg))
                    elif msg == 'R0':
                        # msg_queue.put_nowait(format_for('AND', Status.STATUS_TURNING_RIGHT))
                        msg_queue.put_nowait(format_for('ARD', msg))
                    elif msg == 'B0':
                        # msg_queue.put_nowait(format_for('AND', Status.STATUS_REVERSING))
                        msg_queue.put_nowait(format_for('ARD', msg))
                    # start exploration
                    elif msg == 'ES|':
                        log.info('in exploration case')
                        msg_queue.put_nowait(format_for('ARD', 'S0'))
                        # msg_queue.put_nowait(format_for('AND', Status.STATUS_EXPLORING))
                        # msg_queue.put_nowait(format_for('PC', msg))
                    # fastest path
                    elif msg == 'FP|':
                        # to be confirmed
                        # msg_queue.put_nowait(format_for('AND', Status.STATUS_FASTEST_PATH))
                        log.info('in fastest path case')
                        msg_queue.put_nowait(format_for('PC', msg))
                    elif msg == 'WP|': 
                        #WP|[X,Y] -- technically don't need this branch. can just go into `else`
                        log.info('waypoint')
                        msg_queue.put_nowait(format_for('PC', msg))
                    else:
                        msg_queue.put_nowait(format_for('PC', msg))
                        # msg_queue.put_nowait(format_for('ARD', msg)) # for checklist
                    
            except Exception as e:
                log.error('Android read failed: ' + str(e))
                self.android.connect()


    def read_arduino(self, msg_queue):
        while True:
            msg = self.arduino.read()
            if msg is not None and msg != 'Connected':
                log.info('Put message into msg queue from arduino' + str(msg))
                if self.verbose:
                    log.info('Read Arduino: ' + str(msg))
                if msg['target'] == 'android':
                    # sending FP movements
                    msg_queue.put_nowait(format_for('AND', msg['payload']))
                    msg_queue.put_nowait(format_for('PC', msg['payload']))
                else:
                    #sensor data
                    msg_queue.put_nowait(format_for('PC', msg))
                

    def read_pc(self, msg_queue, img_queue):
        while True:
            msg = ''
            data = self.pc.read()
            log.info("Msg from PC: " + str(data))
            
            # Not a full command
            if not isinstance(data, dict):
                msg += data

                # check last char != '!'
                while msg[-1] != '!':
                    data = self.pc.read()
                    log.info("Data in while: " + str(data))
                    if not data:
                        break
                    msg += data
                    if msg is not None:
                        log.info("Complete message from PC: " + str(msg))

                # number of '!' == number of lines/commands
                    # count_lines = 0
                    # for c in msg:
                    #     if c == '!':
                    #         count_lines += 1
                count_lines = msg.count('!') # can try this method
                if count_lines > 1:
                    #WHEN RECEIVING DATA WITH MORE THAN 1 COMMANDS
                    log.info('More than 1 commands in read_pc (! > 1)')
                    msg_list = msg.split("!")
                    log.info("List of message: " + str(msg_list))
                    for i in range(0,len(msg_list) - 1):
                        msg = pcMsgParser(msg_list[i])
                        log.info('Sending message from PC (MORE THAN ONE command) to respective targets')
                        send_from_pc(msg)
                else:
                    log.info('Only received one command')
                    msg_list = msg.split("!")
                    log.info('Else msg_list: ' + str(msg_list))
                    msg = pcMsgParser(msg_list[0])
                    log.info('Sending message from PC (ONE COMMAND) to respective target')
                    send_from_pc(msg)
            else: 
                #WHEN DATA IS A DICTIONARY // ACTUAL COMMAND
                log.info("Data is a dictionary, an actual command: " + str(data))
                log.info('Sending message from PC (ACTUAL COMMAND) to respective target')
                send_from_pc(msg, ard=True)
                # if new func doesn't work just use old one below

                # if data is not None:  
                #     if self.verbose:
                #         log.info('Read PC: ' + str(data['target']) + '; ' + str(data['payload']))
                #     if data['target'] == 'android':
                #         msg_queue.put_nowait(format_for('AND', data['payload']))
                #     elif data['target'] == 'arduino':
                #         if(data['payload'][-1] == "!"):
                #             print("DATA IN EXCLAMATION CONDITION: " + str(data))
                #             msg_queue.put_nowait(format_for('ARD', data['payload'][:-1]))
                #         else:
                #             msg_queue.put_nowait(format_for('ARD', data['payload']))
                #     elif data['target'] == 'rpi':
                #         img_queue.put_nowait(msg['payload'])
                #     elif data['target'] == 'both':
                #         msg_queue.put_nowait(format_for('AND', data['payload']['android']))
                #         msg_queue.put_nowait(format_for('ARD', data['payload']['arduino']))
                #     elif data['target'] == 'all':
                #         msg_queue.put_nowait(format_for('ARD', data['payload']['arduino'][:-1])) # edge case: arduino not removing '!' ?
                #         msg_queue.put_nowait(format_for('AND', data['payload']['android']))
                #         img_queue.put_nowait(format_for('RPI', data['payload']['rpi'])) #input into img queue with 'TP' command IMAGE REC

    def send_from_pc(self, msg, ard=False):
        if msg is not None:
            if self.verbose:
                log.info('Read PC: ' + str(msg['target']) + '; ' + str(msg['payload']))
            if msg['target'] == 'android':
                msg_queue.put_nowait(format_for('AND', msg['payload']))
            elif msg['target'] == 'arduino':
                if ard:
                    # for the edge case when the parser never remove '!'
                    msg_queue.put_nowait(format_for('ARD', msg['payload'][:-1]))
                else:
                    msg_queue.put_nowait(format_for('ARD', msg['payload']))
            elif msg['target'] == 'rpi':
                img_queue.put_nowait(msg['payload'])
            elif msg['target'] == 'both':
                msg_queue.put_nowait(format_for('AND', msg['payload']['android']))
                msg_queue.put_nowait(format_for('ARD', msg['payload']['arduino']))
            elif msg['target'] == 'all':
                if ard:
                    msg_queue.put_nowait(format_for('ARD', msg['payload']['arduino'][:-1]))
                else:
                    msg_queue.put_nowait(format_for('ARD', msg['payload']['arduino']))
                msg_queue.put_nowait(format_for('AND', msg['payload']['android']))
                img_queue.put_nowait(format_for('RPI', msg['payload']['rpi'])) #input into img queue with 'TP' command IMAGE REC
                        

    def write_target(self, msg_queue):

        while True:
            if not msg_queue.empty():
                msg = msg_queue.get_nowait()
                log.info("write_target msg: " + str(msg))
                msg = json.loads(msg)
                payload = msg['payload']

                if msg['target'] == 'PC':
                    if self.verbose:
                        log.info('Write PC:' + str(payload))
                    self.pc.write(payload)

                elif msg['target'] == 'AND':
                    if self.verbose:
                        log.info('Write Android:' + str(payload))
                    self.android.write(payload)

                elif msg['target'] == 'ARD':
                    if self.verbose:
                        log.info('Write Arduino:' + str(payload))
                    self.arduino.write(payload)                    
