package Client;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.util.Scanner;

import LoggerUtil.LoggerUtil;

public class Client {
	private Socket socket;
	private boolean exitSocket = false;
	private static final Scanner scanner = new Scanner(System.in);
	
	Client() throws Exception{
		System.out.println("Enter the address you wish to connect to: (i.e ipAddress:Port) ");
		String input = scanner.nextLine();
		String[] inputElements = input.split(":");
		String serverAddress = inputElements[0];
		int port = Integer.parseInt(inputElements[1]);
		
		if(isValidatedAddressAndPort(serverAddress, port)) {
			//ouverture du socket
			socket = new Socket(serverAddress, port);
			System.out.format("Serveur lance sur [%s:%d]", serverAddress, port);
			
			//on recupere le input stream pour pouvoir recevoir la reponse du serveur
			DataInputStream in = new DataInputStream(socket.getInputStream());
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			
			String helloMessageFromServer = in.readUTF();
			System.out.println(helloMessageFromServer);
		}
	}
	
	public static boolean isValidatedAddressAndPort(String address, int port) {
		try {
			InetAddress validatedAddress = InetAddress.getByName(address);
		}
		catch(Exception error){
			System.out.println("The ip address given is invalid");
			return false;
			
		}
		if(port >= 5000 && port <= 5050) {
			return true;
		}
		System.out.println("The port given is invalid (outside of range (5000-5050))");
		
		return false;
	}
	
	public void run() {
		while(!this.exitSocket) {
			String command = getNextCommand();
			sendCommand(command);
    	}
	}
	
	public void sendCommand(String command) {
		DataOutputStream out;
		try {
			out = new DataOutputStream(socket.getOutputStream());
	    	out.writeUTF(command);
	    	out.flush();
	    } catch (IOException e) {
	        System.out.println("Failed to send command: " + e.getMessage());
	    }
	}
	public static String getNextCommand() {
		String input = scanner.nextLine();
		return input;
	}

	
	public static void main(String[] args) throws Exception {
		new Client().run();
	    	   //127.0.0.1:5000
	}
}
