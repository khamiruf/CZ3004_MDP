import socket
import sys
import time

'''
Just note that sending of messages has to be within the same session
Host on test Rpi: 192.168.8.1
'''

host = '192.168.8.1'
port = 5182

print('# Creating socket')
try:
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
except socket.error:
    print('Failed to create socket')
    sys.exit()

print('# Getting remote IP address') 
try:
    remote_ip = socket.gethostbyname(host)
except socket.gaierror:
    print('Hostname could not be resolved. Exiting')
    sys.exit()

# Connect to remote server
print('# Connecting to server, ' + host + ' (' + remote_ip + ')')
s.connect((remote_ip , port))

# Send initial message
# print('# Sending "A" to remote server')
# msg = "A\n"
# try:
#     s.sendall(msg.encode('utf-8'))
# except socket.error:
#     print ('Send failed')
#     sys.exit()

while True:
    # pass
    # Check if there's any incoming messages
    time.sleep(1)
    msg = input("Enter command: ")
    s.sendall(msg.encode('utf-8'))

    time.sleep(5)
    read = s.recv(2048).strip().decode("UTF-8")
    print(read)
    