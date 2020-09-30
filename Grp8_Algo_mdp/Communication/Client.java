package Communication;

import java.util.Scanner;

//For testing of connection with RPI
public class Client {

	
	public static void main(String[] args) {
		
		TCPComm tcpObj = new TCPComm();
		tcpObj.establishConnection();
		System.out.println("Start");
		Scanner sc = new Scanner(System.in);
		String enteredMsg = "";
		
		while(! enteredMsg.equals("end")) {
			System.out.println("Enter message: ");
			enteredMsg = sc.nextLine();
			tcpObj.sendMessage(enteredMsg);
			System.out.println("Waiting for reply..");
			tcpObj.readMessage();
		}		
		//tcpObj.closeConnection();
	}
}
