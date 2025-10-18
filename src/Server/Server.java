package Server;
import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.time.LocalTime;
import java.util.Scanner;
import Server.ClientData;

public class Server {
	private static ServerSocket Listener;
	
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
	
	
	public static void main(String[] args) throws Exception{
		
		int clientNumber = 0;

		Scanner inputStream = new Scanner(System.in);
		System.out.println("Enter the address you wish to connect to: (i.e ipAddress:Port) ");
		String input = inputStream.nextLine();
		String[] inputElements = input.split(":");
		String serverAddress = inputElements[0];
		int serverPort = Integer.parseInt(inputElements[1]);
		inputStream.close();
		
		File rootDir = new File(System.getProperty("user.dir") + File.separator + "server_storage");
		if (!rootDir.exists()) {
		    rootDir.mkdirs();
		}
		
		if(isValidatedAddressAndPort(serverAddress, serverPort)) {
			
			Listener = new ServerSocket();
			Listener.setReuseAddress(true);
			InetAddress serverIP = InetAddress.getByName(serverAddress);
			
			Listener.bind(new InetSocketAddress(serverIP, serverPort));
			
			System.out.format("The server is running on %s:%d%n", serverAddress, serverPort);
			
			try {
				while(true) {
					new ClientHandler(Listener.accept(), clientNumber++, rootDir).start();
				}
			}
			finally {
				Listener.close();
			}
		}
		
		
	}

}
