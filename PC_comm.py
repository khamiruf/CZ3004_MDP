import socket
import time
import sys

class PC_conn(object):

	def __init__(self):
		self.tcp_ip = "192.168.8.1"
		self.port = 5182
		self.conn = None
		self.client = None
		self.add = None
		self.pc_connect = False

	def close_pc_socket(self):
		if self.conn:
			self.conn.close()
			print("Closing server socket")
		if self.client:
			self.client.close()
			print("Closing client socket")
			self.pc_connect = False

	def pc_is_connected(self):
		return self.pc_connect

	def connect_pc(self):
		try:
			self.conn = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
			self.conn.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1) # set socket options (necessary to retry connection / reuse addr)
			self.conn.bind((self.tcp_ip, self.port)) 
			self.conn.listen(1) # listen for incoming connections
			print("Listening for incoming connections from PC...")
			self.client, self.add = self.conn.accept()
			print("Connected! Connection address: ", self.add)
			self.pc_connect = True
		except socket.error as e:
			print("Error: %s" % str(e))
			print("Try again in a few seconds")

	def send_to_pc(self, message):
		try:
			self.client.sendto(str.encode(message + '\n'), self.add)
			print("Sent [%s] to PC" % message)
		except TypeError:
			print("Error: Null value cannot be sent")

	def recv_fr_pc(self):
		try:
			pc_data = self.client.recv(2048)
			return pc_data
		except Exception as e:
			print("Error: %s" % str(e))
			print("Value not read from PC")

if __name__ == "__main__":
	try:
		print("main")
		pc = PC_conn()
		pc.connect_pc()
		
		send_msg = input()
		print("send_to_pc(): %s" % send_msg)
		pc.send_to_pc(send_msg)

		# read
		msg = pc.recv_fr_pc()
		print("Data received: %s" % msg.decode("utf-8"))
		
		print("closing sockets")
		pc.close_pc_socket()
	except KeyboardInterrupt:
		print("Interrupted")
		sys.exit(0)
		
