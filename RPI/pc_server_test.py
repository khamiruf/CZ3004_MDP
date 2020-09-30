from src.communicator.PC import PC
from src.communicator.Android import Android
import time

#android = Android()
#android.connect()

pc = PC()
pc.connect()

while True:
    try:
        sc_msg = input("Enter start coord:")
        pc.write(sc_msg)
        send_msg = input("Enter something: ") # sensor data
        pc.write(send_msg)

        buffer = ''
        msg = pc.read()
        buffer += msg

        print(buffer)

        while buffer[:-1] != '!':
            msg = pc.read()
            if not msg:
                break
            buffer += msg
            if buffer is not None:
                print('Message from PC: ' + buffer)
                print(str(buffer))
            # pc.write(str(msg))
        if pc.client_sock is None:
            print('pc was disconnected')
    except Exception as e:
        print(e)