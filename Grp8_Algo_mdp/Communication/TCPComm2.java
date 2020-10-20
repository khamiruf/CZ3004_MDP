package Communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPComm2 {

	// initialize socket and input output streams 
    private Socket socket = null; 
    private InputStream  din = null; 
    private PrintStream dout = null;
    private static TCPComm2 cs = null;
    
    
    private static final String IPaddr = "192.168.8.1";
	private static final int portNum = 5182;
    
    private TCPComm2() {
    	
    }
    
    public static TCPComm2 getInstance() {
    	if (cs == null) {
    		cs = new TCPComm2();
    		//cs.connectToRPI();
    	}
    	return cs;
    }
    
    public static boolean checkConnection() {
    	if (cs == null) {
    		return false;
    	}
    	return true;
    }
    
    public String establishConnection() {
    	String msg = "";
    	if (socket == null) {
	    	try {
	    		socket = new Socket(IPaddr, portNum);	
	    		System.out.println("Connected to " + IPaddr + ":" + Integer.toString(portNum));
	    		din  = socket.getInputStream(); 
	    		dout = new PrintStream(socket.getOutputStream()); 
	    		
	    		
	    	}
	    	catch(UnknownHostException ex) { 
	    		System.out.println("UnknownHostException in ConnectionSocket connectToRPI Function"); 
	    		msg = "Failed to Connect RPI : " + ex.getMessage();
	        } 
	    	catch (IOException ex) {
	    		System.out.println("IOException in ConnectionSocket connectToRPI Function");
	    		
	    		msg = "Failed to Connect RPI : " + ex.getMessage();
	    	}
    	}
    	return msg;
    }
    
    public void sendMessage(String message) {
    	try {
    		//System.out.println("Size:" + message.getBytes().length+":"+message.getBytes());
    		dout.write(message.getBytes());
    		dout.flush();
    		
    		//if (debug.get()) {
    		//	System.out.println('"' + message + '"' + " sent successfully");
    		//}
    	}
    	catch (IOException IOEx) {
    		System.out.println("IOException in ConnectionSocket sendMessage Function");
    	}
    }
    
    // Get message from buffer
    public String readMessage() throws InterruptedException {

    	byte[] byteData = new byte[512];
    	try {
    		int size = 0;
//    		while (din.available() == 0 && connected.get()) {
//    			try {
//    				ConnectionManager.getInstance().join(1);
//    			}
//    			catch(Exception e) {
//    				System.out.println("Error in receive message");
//    			}
//    		}
    		din.read(byteData);
    		
    		// This is to get rid of junk bytes
    		while (size < 512) {
    			if (byteData[size] == 0) {
    				break;
    			}
    			size++;
    		}
    		String message = new String(byteData, 0, size, "UTF-8");

    		return message;
    	}
    	catch (IOException IOEx) {
    		System.out.println("IOException in ConnectionSocket receiveMessage Function");
    		throw new InterruptedException("TCP ReadMsg() Exception");
    	}
   
    	
    }
    
    public void closeConnection() {
    	if (socket != null) {
    		try {
    			socket.close();
    			din.close();
    			dout.close();
    			dout = null;
    			socket = null;
    			din = null;
    			
    			System.out.println("Successfully closed the ConnectionSocket.");
    		}
    		catch (IOException IOEx) {
        		System.out.println("IOException in ConnectionSocket closeConnection Function");
        	}
    	}
    }
    /*
    public static void setDebugTrue() {
    	debug.set(true);
    }
    
    public static boolean getDebug() {
    	return debug.get();
    }
	*/
	
}
