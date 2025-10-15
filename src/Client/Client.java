package Client;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.InetAddress;
import java.util.Scanner;

import LoggerUtil.LoggerUtil;

public class Client {
	private static Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
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
		while(!exitSocket) {
			String command = getNextCommand();
			ExecuteCommand(command);
		}
	}
	
	public void ExecuteCommand(String command) {
		DataOutputStream out;
		try {
			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());
			String[] parts = command.split(" ", 2);
			switch(parts[0]) {
				
			case "exit":
				LoggerUtil.log(socket, command);
            	out.writeUTF(command);
            	out.flush();
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
	            	
	                String response = in.readUTF();
	                System.out.println(response);
	            } catch (IOException e) {
	                System.out.println("Failed to send command: " + e.getMessage());
	            }
				break;
			}	
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}
	
	public void uploadFile(String filePath) {
		File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            System.out.println("Invalid file path.");
            return;
        }
        try {
            String fileName = file.getName();
            out.writeUTF("upload " + fileName);
            out.flush();
            String response = in.readUTF();
            if (!"READY_FOR_UPLOAD".equals(response)) {
                System.out.println("Server refused upload: " + response);
                return;
            }
            
            long fileSize = file.length();
            out.writeLong(fileSize);
            
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
//                long sent = 0;

                while ((bytesRead = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
//                    sent += bytesRead;
//                    int percent = (int)((sent * 100) / fileSize);
//                    System.out.print("\rUploading... " + percent + "%");
                }
            }
            out.flush();
            System.out.println("\n" + in.readUTF());
        }
        catch(IOException e) {
        	System.out.println("Upload failed: " + e.getMessage());
        }
		
	}
	public void downloadFile() {
		
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
