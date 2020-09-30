import serial
import time
from src.Logger import Logger
from src.config import SERIAL_PORT
from src.config import BAUD_RATE
from src.config import LOCALE
from src.communicator.utils import ardMsgParser

log = Logger()

# Arduino will need an accompanying script to receive the data from Rpi
# Communication has to be two ways, Rpi send, Arduino receive and reply, Rpi receive

# arduino = Arduino()
# arduino.connect()
# arduino.write(msg)
# while True:
#     msg = arduino.read()
#     print(msg)

class Arduino:
    def __init__(self, serial_port=SERIAL_PORT, baud_rate=BAUD_RATE):
        self.serial_port = serial_port
        self.baud_rate = baud_rate
        self.connection = None

    def show_settings(self):
        print('Serial Port: ' + str(self.serial_port))
        print('Baud Rate: ' + str(self.baud_rate))
        print('Connection Established: ' + str(self.connection is not None))

    def connect(self):
        log.info('Establishing connection with Arduino')
        try:
            self.connection = serial.Serial(self.serial_port, self.baud_rate, timeout=3)
            time.sleep(3)
            if self.connection is not None:
                log.info('Successfully connected with Arduino: ' + str(self.connection.name))

        except Exception as error:
            log.error('Connection with Arduino failed: ' + str(error))

    def disconnect(self):
        try:
            self.connection.close()
            log.info('Successfully closed connection with Arduino')
        except Exception as error:
            log.error('Arduino close connection failed: ' + str(error))
    
    def write(self, msg):
        try:
            self.connection.write(str.encode(msg))
            log.info('Successfully wrote message to Arduino')
        except Exception as error:
            log.error('Arduino write failed: ' + str(error))

    buff=[]
    buffFlag: bool

    def read(self):
        try:
            # msg = self.connection.readline().decode(LOCALE).rstrip()
            self.connection.flush() # flush before reading
            msg = self.connection.readline().decode('utf-8').rstrip()
            # print("from arduino.py", msg)

            if len(msg) > 0:
                print("read from arduino msg", msg)
#                if(msg[0]=='@'):
 #                   setBufFlag(True)
  #              elif(msg[0]!='!' and getBufFlag()):
   #                 buff.append(msg[0])
    #            elif(msg[0]=='!' and getBufFlag()):
     #               setBufFlag(False)
      #              print("BUFFER COMPLETE", buff)
       #             return buff
                
        #        print("Buffer", buff)
                return ardMsgParser(msg)
            return None       
        except Exception as error:
            log.error('Arduino read failed: ' + str(error))

    def getBufFlag(self):
        return buffFlag
    
    def setBufFlag(self, flag):
        buffFlag = flag
