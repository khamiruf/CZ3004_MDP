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
	private static TCPComm tcpObj = null;
	private Socket clientSocket;
	private DataOutputStream outputStream;
	private BufferedReader inputStream;

	
	private TCPComm() {}
	
	public static TCPComm getInstance() {
		if (tcpObj == null) {
			tcpObj = new TCPComm();
		}

		return tcpObj;
	}
	
	public String establishConnection() {
		String msg ="";
		try {
						
			clientSocket = new Socket(IPaddr, portNum);
			this.outputStream = new DataOutputStream(clientSocket.getOutputStream());
			this.inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (Exception ex) {
			
			msg = "Failed to Connect RPI : " + ex.getMessage();
		}
		
		
		return msg;
	}

	public void closeConnection() {
		
		if(clientSocket!=null)
		System.out.println("Socket status before close: " + clientSocket.isClosed());
		else {
			System.out.println("Socket status before close: socket is null");
		}
		if(clientSocket!=null) { 
			try { 
			 clientSocket.close();
			 outputStream.close();
			 inputStream.close();
			 clientSocket=null; 
			 outputStream=null; 
			 inputStream=null;
		  System.out.println("Successfully closed connection.");
		  
		  } catch (IOException ex) {
		  
		  System.out.println("Closing connection error: " + ex.getMessage()); }
		  
		  }
		 
	}

	public String sendMessage(String msg) {
		String rmsg="";
		try {
			this.outputStream.writeBytes(msg+"!");
			//this.outputStream.flush();
			//System.out.println("Sending msg: " + msg);
			rmsg = msg;
		} catch (Exception ex) {
			System.out.println("TCPComm sendmsg() Exception: " + ex.getMessage());
			rmsg = ex.getMessage();
		}
		
		return rmsg;
	}
	
	/*
	public String readMessage() throws InterruptedException{
		String receivedMsg = null;

		try {
			do {
				receivedMsg = this.inputStream.readLine();
			
				}
			while(receivedMsg==null||receivedMsg.length()==0);
		}
		catch (Exception ex) {
			System.out.println("Error readMsg(): " + ex.getMessage());
			throw new InterruptedException("Error readMsg(): " + ex.getMessage());	
		}
		//System.out.println("ReturnBackThread--");
		return receivedMsg;
	}
	*/
	
	public String readMessage() throws InterruptedException {
		String receivedMsg="";
		try {
			receivedMsg = this.inputStream.readLine();
		} catch (IOException ex) {
			//e.printStackTrace();
			System.out.println("TCP ReadMsg() Exception: "+receivedMsg+":" + ex.getMessage());
			throw new InterruptedException("TCP ReadMsg() Exception");
		}
		return receivedMsg;
		
	}
	
}
