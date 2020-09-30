package Communication;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import gui.MainGUI;

public class TCPComm {

	private static final String IPaddr = "192.168.8.1";
	private static final int portNum = 5182;

	//private static TCPComm tcpObj = null;
	private Socket clientSocket;
	private DataOutputStream outputStream;
	private BufferedReader inputStream;

	
	public TCPComm() {
	}
	/*
	public static TCPComm getInstance() {
		if (tcpObj == null) {
			tcpObj = new TCPComm();
		}

		return tcpObj;
	}
	 */
	public String establishConnection() {
		String msg ="";
		try {
			System.out.println("Creating new connection.. :D ");
			
			clientSocket = new Socket(IPaddr, portNum);
			this.outputStream = new DataOutputStream(clientSocket.getOutputStream());
			this.inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (Exception ex) {
			
			System.out.println("Failed to Connect RPI " + IPaddr + " , Error: " + ex.getMessage());
			msg = "Failed to Connect RPI : " + ex.getMessage();
		}
		
		System.out.println("Connected successfully.. :DD ");
		return msg;
	}

	public void closeConnection() {
		/*
		 * if(!clientSocket.isClosed()) { try { clientSocket.close();
		 * outputStream.close(); inputStream.close();
		 * 
		 * clientSocket=null; outputStream=null; inputStream=null;
		 * System.out.println("Successfully closed connection.");
		 * 
		 * } catch (IOException ex) {
		 * 
		 * System.out.println("Closing connection error: " + ex.getMessage()); }
		 * 
		 * }
		 */
	}

	public String sendMessage(String msg) {
		String rmsg="";
		try {
			this.outputStream.writeBytes(msg+"!");
			//this.outputStream.flush();
			System.out.println("Sending msg: " + msg);
			rmsg = msg;
		} catch (Exception ex) {
			System.out.println("Error sending msg: " + ex.getMessage());
			rmsg = ex.getMessage();
		}
		
		
		
		return rmsg;
	}
	
	public String readMessage() {
		String receivedMsg = null;

		try {
			do{
				receivedMsg = this.inputStream.readLine();
				//System.out.println("TCP Received: " + receivedMsg);
				//receivedMsg = this.inputStream.readLine();
				//System.out.println("TCP Received: " + receivedMsg);
			}while (receivedMsg== null || receivedMsg.length()==0);
		} catch (Exception ex) {
			System.out.println("Error receiving msg: " + ex.getMessage());
			receivedMsg = "Error receiving msg:"  + ex.getMessage();	
		}
		//System.out.println("ReturnBackThread--");
		return receivedMsg;
	}
	
	
	/*
	public String readMessage() {
		String receivedMsg = "";

		try {
				if(this.inputStream.ready()) {
					receivedMsg = this.inputStream.readLine();
					System.out.println("TCP Received: " + receivedMsg);
				}
				
				//receivedMsg = this.inputStream.readLine();
				//System.out.println("TCP Received: " + receivedMsg);
			
		} catch (Exception ex) {
			System.out.println("Error receiving msg: " + ex.getMessage());
			receivedMsg = "Error receiving msg:"  + ex.getMessage();	
		}
		System.out.println("ReturnBackThread--");
		return receivedMsg;
	}
	*/
}
