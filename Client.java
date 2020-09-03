import java.io.*;
import java.net.Socket;

public class Client {

    public static final String HOST = "192.168.1.1";    // IP address of Raspberry Pi
    public static final int PORT = 0;                   // Port number of Raspberry Pi

//    private static Socket s;
//    private static DataInputStream din;
//    private static DataOutputStream dout;
//    private static BufferedReader br;

    public static void main(String[] args) {
        Socket s = null;
        try {
            s = new Socket("localhost", 1234);

            DataInputStream din = new DataInputStream(s.getInputStream());
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            String str = "Hello World", str2 = "";
            while (!str.equals("stop")) {
                str = br.readLine();
                dout.writeBytes(str);
                System.out.println("sending: " + str);
                dout.flush();
                str2 = din.readUTF();
                System.out.println("Server says: " + str2);
            }

            dout.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public static void Communication() {
//        try {
//            System.out.println("Initiating connection with " + HOST + " on port " + PORT + ".");
//            s = new Socket(HOST, PORT);
//            System.out.println("Connection successful.");
//            din = new DataInputStream(s.getInputStream());
//            dout = new DataOutputStream(s.getOutputStream());
//            br = new BufferedReader(new InputStreamReader(System.in));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
