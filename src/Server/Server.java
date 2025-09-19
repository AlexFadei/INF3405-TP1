package Server;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

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
		String serverAddress = "127.0.0.1";
		int serverPort = 5000;
		
		Listener = new ServerSocket();
		Listener.setReuseAddress(true);
		InetAddress serverIP = InetAddress.getByName(serverAddress);
		
		Listener.bind(new InetSocketAddress(serverIP, serverPort));
		
		System.out.format("The server is running on %s:%d%n", serverAddress, serverPort);
		
		try {
			while(true) {
				new ClientHandler(Listener.accept(), clientNumber++).start();
			}
		}
		finally {
			Listener.close();
		}
	}

}
