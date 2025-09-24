package Client;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.InetAddress;
import java.util.Scanner;

import LoggerUtil.LoggerUtil;

public class Client {
	private static Socket socket;
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
	
	public void ExecuteCommand(String command) {
		DataOutputStream out;
		try {
			out = new DataOutputStream(socket.getOutputStream());
			String[] parts = command.split(" ", 2);
			switch(parts[0]) {
				
			case "exit":
				LoggerUtil.log(socket, command);
				System.out.format("Disconnecting [%s:%d]");
				try {
					socket.close();
				}
				catch(IOException e) {}
				
				break;
			case "upload":
				LoggerUtil.log(socket, command);
				
				break;
			case "download":
				LoggerUtil.log(socket, command);
				
				break;
			default:
	            try {
	            	out.writeUTF(command);
	            	out.flush();
	            } catch (IOException e) {
	                System.out.println("Failed to send command: " + e.getMessage());
	            }
				break;
			}	
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}
	public static String getNextCommand() {
		Scanner inputStream = new Scanner(System.in);
		String input = inputStream.nextLine();
		inputStream.close();
		return input;
	}

	
	public static void main(String[] args) throws Exception {
		Scanner inputStream = new Scanner(System.in);
		System.out.println("Enter the address you wish to connect to: (i.e ipAddress:Port) ");
		String input = inputStream.nextLine();
		String[] inputElements = input.split(":");
		String serverAddress = inputElements[0];
		int port = Integer.parseInt(inputElements[1]);
		inputStream.close();
		
		if(isValidatedAddressAndPort(serverAddress, port)) {
			//ouverture du socket
			socket = new Socket(serverAddress, port);
			System.out.format("Serveur lance sur [%s:%d]", serverAddress, port);
			
			//on recupere le input stream pour pouvoir recevoir la reponse du serveur
			DataInputStream in = new DataInputStream(socket.getInputStream());
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			
			String helloMessageFromServer = in.readUTF();
			System.out.println(helloMessageFromServer);
			socket.close();
		}
		
		
		
		
		
	}
}
