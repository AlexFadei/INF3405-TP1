package Client;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
		System.out.println("Entrez l'adresse à laquelle vous souhaitez vous connectez : (ipAddress:Port)");
		String input = scanner.nextLine();
		String[] inputElements = input.split(":");
        if (inputElements.length < 2) {
            System.out.println("Invalid input. Please enter IP and port like this: 127.0.0.1:5000");
            continue;
        }
		String serverAddress = inputElements[0];
		int port = Integer.parseInt(inputElements[1]);
		if(isValidatedAddressAndPort(serverAddress, port)) {
		    socket = new Socket(serverAddress, port);
		    System.out.format("Serveur lancé sur [%s:%d]\n", serverAddress, port);
		    
		    this.in = new DataInputStream(socket.getInputStream());
		    this.out = new DataOutputStream(socket.getOutputStream());
		    
		    String helloMessageFromServer = this.in.readUTF();
		    System.out.println(helloMessageFromServer);
		    break;
		}
    	
    }
    }
    
    
	public static boolean isValidatedAddressAndPort(String address, int port) {
		try {
			InetAddress validatedAddress = InetAddress.getByName(address);
		}
		catch(Exception error){
			System.out.println("L'adresse IP donnée est invalide");
			return false;
			
		}
		if(port >= 5000 && port <= 5050) {
			return true;
		}
		System.out.println("Le port donné est invalide (hors de l'intervalle (5000-5050))");
		
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
			String[] parts = command.split(" ", 2);
			switch(parts[0]) {
				
			case "exit":
				LoggerUtil.log(socket, command);
            	this.out.writeUTF(command);
            	this.out.flush();
				System.out.format("Déconnecte [%s:%d]");
				try {
					socket.close();
				}
				catch(IOException e) {}
				
				break;
			case "upload":
	            if (parts.length > 1) {
	                uploadFile(parts[1].trim());
	            } else {
	                System.out.println("Utilisation: upload <chemin-fichier>");
	            }
	            break;
			case "download": 
				String filename = command.substring(9).trim();
			    downloadFile(filename);
			default:
	            try {
	            	this.out.writeUTF(command);
	            	this.out.flush();
	            	
	                String response = in.readUTF();
	                System.out.println(response);
	            } catch (IOException e) {
	                System.out.println("Echec dans l'envoi du message : " + e.getMessage());
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
            System.out.println("Chemin de fichier invalide.");
            return;
        }
        try {
            String fileName = file.getName();
            out.writeUTF("upload " + fileName);
            out.flush();
            String response = in.readUTF();
            if (!"READY_FOR_UPLOAD".equals(response)) {
                System.out.println("Le serveur a refusé l'upload: " + response);
                return;
            }
            
            long fileSize = file.length();
            out.writeLong(fileSize);
            
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            out.flush();
            System.out.println("\n" + in.readUTF());
        }
        catch(IOException e) {
        	System.out.println("Erreur d'upload: " + e.getMessage());
        }
		
	}
	private void downloadFile(String fileName) {
	    try {
	        out.writeUTF("download " + fileName);
	        out.flush();

	        String response = in.readUTF();
	        if (response.startsWith("ERROR")) {
	            System.out.println(response);
	            return;
	        }

	        if (!"READY_FOR_DOWNLOAD".equals(response)) {
	            System.out.println("Réponse inattendu du serveur : " + response);
	            return;
	        }

	        long fileSize = in.readLong();
	        File target = new File(fileName);

	        try (FileOutputStream fos = new FileOutputStream(target)) {
	            byte[] buffer = new byte[4096];
	            long totalRead = 0;
	            int bytesRead;
	            while (totalRead < fileSize &&
	                    (bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalRead))) != -1) {
	                fos.write(buffer, 0, bytesRead);
	                totalRead += bytesRead;
	            }
	        }

	        String confirmation = in.readUTF();
	        System.out.println(confirmation);
	    } catch (IOException e) {
	        System.out.println("Erreur lors du download: " + e.getMessage());
	    }
	}

	
	public static String getNextCommand() {
		String input = scanner.nextLine();
		return input;
	}

	
	public static void main(String[] args) throws Exception {
		new Client().run();
	}
}
