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
        self.detector = SymbolDetector()

        # queues
        self.msg_queue = Queue()
        self.img_queue = Queue()

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
            
            log.info('Launching Symbol Detector')
            # self.detector()

            log.info('Multiprocess Communication Session Started')

            count = 20
            while True:
                if not self.img_queue.empty():
                    print("img queue not empty")
                    msg = self.img_queue.get_nowait()
                    msg = json.loads(msg)
                    payload = msg['payload']
                    print("msg: ", msg)
                    if payload == 'TP':
                        print("calling img detector")
                        self.detector.main(count=count)
                        log.info("Successfully took a picture")
                        count += 1
                        # log.info('Detecting for Symbols')
                        # frame = self.detector.get_frame()
                        # symbol_match = self.detector.detect(frame)
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
        
        # time.sleep(1)
        # self._allow_reconnection()

    def end(self):
        log.info('Multiprocess Communication Session Ended')

    def _allow_reconnection(self):
        # log.info('You can reconnect to RPi after disconnecting now')

        while True:
            try:
                # if self.android.client_sock is None:
                #     log.info("android was disconnected from rpi")
                #     self._reconect_android()


                # if not self.read_android_process.is_alive():
                #     self._reconect_android()
                
                # can try this
                if self.pc.client_sock is None:
                    log.info("pc was disconnected from rpi")
                    self._reconnect_pc()

                # doesnt actually detect the disconnection
                if not self.read_pc_process.is_alive():
                    log.info("read pc process is not alive!")
                    self._reconnect_pc()

                # if not self.read_arduino_process.is_alive():
                #     self._reconnect_arduino()
                # if self.arduino.connection is None:
                #     log.info("arduino was disconnected from rpi")
                #     self._reconnect_pc()
            except Exception as error:
                log.error("Error during reconnection: ", error)
                raise error
    
    def _reconect_android(self):
        self.android.disconnect()
        self.read_android_process.terminate()

        self.android.connect()

        self.read_android_process = Process(target=self.read_android, args=(self.msg_queue,))
        self.read_android_process.start()
        
        log.info("Reconnected to Android")

    def _reconnect_pc(self):
        log.info("PC disconnected")
        self.pc.disconnect()
        self.read_pc_process.terminate()
        
        self.pc.connect()

        self.read_pc_process = Process(target=self.read_pc, args=(self.msg_queue,))
        self.read_pc_process.start()

        log.info("Reconnected to PC")

    def _reconnect_arduino(self):
        self.arduino.disconnect()
        self.read_arduino_process.terminate()
        
        self.arduino.connect()

        self.read_arduino_process = Process(target=self.read_arduino, args=(self.msg_queue,))
        self.read_arduino_process.start()

        log.info("Reconnected to Arduino")


    def read_android(self, msg_queue):
        while True:
            try:
                msg = self.android.read()
                if msg is not None:
                    if self.verbose:
                        log.info('Read Android: ' + str(msg))
                    # for checklist -- remote commands
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
                        # to be confirmed
                        log.info('in exploration case')
                        msg_queue.put_nowait(format_for('ARD', 'S0'))
                        # msg_queue.put_nowait(format_for('AND', Status.STATUS_EXPLORING))
                        # msg_queue.put_nowait(format_for('PC', msg))
                    elif msg == 'FP|':
                        # to be confirmed
                        # msg_queue.put_nowait(format_for('AND', Status.STATUS_FASTEST_PATH))
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
            # log.info("msg from arduino: " + msg)
            if msg is not None and msg != 'Connected':
                if self.verbose:
                    log.info('Read Arduino: ' + str(msg))
                msg_queue.put_nowait(format_for('PC', msg))
                log.info('put message into msg queue from arduino')
                log.info(msg)

    def read_pc(self, msg_queue, img_queue):
        while True:
            msg = ''
            data = self.pc.read()
            print("data string: ", data)
            if not isinstance(data, dict):
                msg += data

                while msg[-1] != '!':
                    data = self.pc.read()
                    print("data in while: ", data)
                    if not data:
                        break
                    msg += data
                    if msg is not None:
                        log.info("Message from PC:")
                        log.info(msg)

                count_lines = 0
                for c in msg:
                    if c == '!':
                        count_lines += 1
                if count_lines > 1:
                    print('more than 1 exclamation (if)')
                    msg_list = msg.split("!")
                    print(msg_list)
                    for i in range(0,len(msg_list) - 1):
                        print(msg_list[i])
                        msg = pcMsgParser(msg_list[i])

                        if msg is not None:
                            if self.verbose:
                                log.info('Read PC: ' + str(msg['target']) + '; ' + str(msg['payload']))
                            if msg['target'] == 'android':
                                msg_queue.put_nowait(format_for('AND', msg['payload']))
                            elif msg['target'] == 'arduino':
                                msg_queue.put_nowait(format_for('ARD', msg['payload']))
                            elif msg['target'] == 'rpi':
                                img_queue.put_nowait(msg['payload'])
                            elif msg['target'] == 'both':
                                msg_queue.put_nowait(format_for('AND', msg['payload']['android']))
                                msg_queue.put_nowait(format_for('ARD', msg['payload']['arduino']))
                            elif msg['target'] == 'all':
                                msg_queue.put_nowait(format_for('ARD', msg['payload']['arduino']))
                                msg_queue.put_nowait(format_for('AND', msg['payload']['android']))
                                img_queue.put_nowait(format_for('RPI', msg['payload']['rpi'])) #input into img queue with 'TP' command
                else:
                    print('in else of read_pc')
                    msg_list = msg.split("!")
                    print("else msg_list: ", msg_list)
                    msg = pcMsgParser(msg_list[0])

                    if msg is not None:
                        if self.verbose:
                            log.info('Read PC: ' + str(msg['target']) + '; ' + str(msg['payload']))
                        if msg['target'] == 'android':
                            msg_queue.put_nowait(format_for('AND', msg['payload']))
                        elif msg['target'] == 'arduino':
                            msg_queue.put_nowait(format_for('ARD', msg['payload']))
                        elif msg['target'] == 'rpi':
                            img_queue.put_nowait(msg['payload'])
                        elif msg['target'] == 'both':
                            msg_queue.put_nowait(format_for('AND', msg['payload']['android']))
                            msg_queue.put_nowait(format_for('ARD', msg['payload']['arduino']))
                        elif msg['target'] == 'all':
                            msg_queue.put_nowait(format_for('ARD', msg['payload']['arduino']))
                            msg_queue.put_nowait(format_for('AND', msg['payload']['android']))
                            img_queue.put_nowait(format_for('RPI', msg['payload']['rpi'])) #input into img queue with 'TP' command
            else:
                if msg is not None:
                        if self.verbose:
                            log.info('Read PC: ' + str(msg['target']) + '; ' + str(msg['payload']))
                        if msg['target'] == 'android':
                            msg_queue.put_nowait(format_for('AND', msg['payload']))
                        elif msg['target'] == 'arduino':
                            msg_queue.put_nowait(format_for('ARD', msg['payload']))
                        elif msg['target'] == 'rpi':
                            img_queue.put_nowait(msg['payload'])
                        elif msg['target'] == 'both':
                            msg_queue.put_nowait(format_for('AND', msg['payload']['android']))
                            msg_queue.put_nowait(format_for('ARD', msg['payload']['arduino']))
                        elif msg['target'] == 'all':
                            msg_queue.put_nowait(format_for('ARD', msg['payload']['arduino']))
                            msg_queue.put_nowait(format_for('AND', msg['payload']['android']))
                            img_queue.put_nowait(format_for('RPI', msg['payload']['rpi'])) #input into img queue with 'TP' command

    def write_target(self, msg_queue):

        while True:
            if not msg_queue.empty():
                msg = msg_queue.get_nowait()
                print(msg)
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
                    