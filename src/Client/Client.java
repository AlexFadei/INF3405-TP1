package Client;
import java.io.DataInputStream;
import java.net.Socket;
import java.net.InetAddress;
import java.util.Scanner;

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

	
	public static void main(String[] args) throws Exception {
		Scanner inputStream = new Scanner(System.in);
		System.out.println("Enter the address you wish to connect to: (i.e ipAddress:Port) ");
		String input = inputStream.nextLine();
		String[] inputElements = input.split(":");
		String serverAddress = inputElements[0];
		int port = Integer.parseInt(inputElements[1]);
		
		if(isValidatedAddressAndPort(serverAddress, port)) {
			//ouverture du socket
			socket = new Socket(serverAddress, port);
			System.out.format("Serveur lance sur [%s:%d]", serverAddress, port);
			
			//on recupere le input stream pour pouvoir recevoir la reponse du serveur
			DataInputStream in = new DataInputStream(socket.getInputStream());
			
			String helloMessageFromServer = in.readUTF();
			System.out.println(helloMessageFromServer);
			socket.close();
		}
		
		
		
		
		
	}
}
