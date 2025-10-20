package Server;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.LocalTime;
import LoggerUtil.LoggerUtil;

//worker thread for the client
public class ClientHandler extends Thread {
	private File currentDir;
	private Socket socket; private int clientNumber;
	private boolean exitSocket;
	private final File rootDir; 
	
	public ClientHandler(Socket socket, int clientNumber, File rootDir) {
        this.socket = socket;
        this.clientNumber = clientNumber;
        this.currentDir = rootDir;
        this.rootDir = rootDir;
        System.out.println(rootDir);
        System.out.println("New connection with client#" + clientNumber + " at " + socket);
    }

	public void ExecuteCommand(String command, DataInputStream in, DataOutputStream out) throws IOException {
		String[] parts = command.split(" ", 2);
		String cmd = parts[0];
		String arg = parts.length > 1 ? parts[1] : "";
		LoggerUtil.log(this.socket, command);
		
		switch(cmd) {
				
		case "ls": handleLs(); break;
			
		case "cd": handleCd(arg.trim()); break;
			
		case "mkdir": handleMkDir(arg.trim()); break;
			
		case "upload": handleUpload(arg.trim(), in, out); break;
			
		case "download": handleDownload(arg.trim(), out); break;
			
		case "delete": handleDelete(arg.trim()); break;
		
		case "exit":
			this.sendStringToClient("Vous avez été déconnecté");
			
			this.exitSocket = true;
			break;
		default:
			this.sendStringToClient("Unrecognised Command, please try again");
			break;
		}
		
	}
	public boolean handleLs() {
		File[] listFiles = this.currentDir.listFiles();
		StringBuilder outputList = new StringBuilder("");
		if(listFiles == null) {
			return false;
		}
		for(File file : listFiles) {
			outputList.append(file.isDirectory()? "[Folder]" : "[File]");
			outputList.append(file.getName());
			
			outputList.append("\n");
		}
		this.sendStringToClient(outputList.toString());
		
		return true;
		};
	public boolean handleCd(String arg) {
		if(arg.isEmpty()) {
			this.sendStringToClient("invalid directory");
			return false;
		}
		if (arg.equals("..")) {
	        if (currentDir.equals(rootDir)) {
	            this.sendStringToClient("Already at root directory");
	            return true;
	        }

	        currentDir = currentDir.getParentFile();
	        this.sendStringToClient("Changed directory to: " + currentDir.getAbsolutePath());
	        return true;
	    }
		File requestedFileDirectory = new File(this.currentDir, arg).getAbsoluteFile();
		if( requestedFileDirectory.exists() &&  requestedFileDirectory.isDirectory()) {
			this.currentDir =  requestedFileDirectory;
			this.sendStringToClient("changed directory to: " + arg);
		}
		
		
		return true;};
	public boolean handleMkDir(String name) {
		if(name.isEmpty()) {
			this.sendStringToClient("invalid name");
			return false;
		}
		File createdFile = new File(this.currentDir, name);
	    if (createdFile.mkdir()) {
	    	this.sendStringToClient("Le dossier " + createdFile.getName() + " a été créé.");
	    } else {
	        this.sendStringToClient( "erreur dans la création du dossier " + name);
	    }
		
		
		return true;};
	public boolean handleDelete(String name) {
		if(name.isEmpty()) {
			this.sendStringToClient("nom invalide");
			return false;
		}
		File target = new File(this.currentDir, name);
		if(!target.exists()) {
			this.sendStringToClient( "le dossier/fichier " + name + " n'existe pas.");
			return false;
		}
	    boolean isDirectory = target.isDirectory() ? target.delete() : target.delete();
	    if (isDirectory) {
	        this.sendStringToClient("le dossier " + name + " a été supprimé");
	    } else {
	        this.sendStringToClient("le fichier " + name + " a été supprimé");
	    }
		
		return true;};
		
	private void handleUpload(String fileName, DataInputStream in, DataOutputStream out) throws IOException {
        if (fileName.isEmpty()) {
            out.writeUTF("Invalid parameters for upload.");
            return;
        }

        File target = new File(currentDir, fileName);
        out.writeUTF("READY_FOR_UPLOAD");

        long fileSize = in.readLong();
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

        out.writeUTF("Upload complete: " + fileName);
        System.out.println("File uploaded by client#" + clientNumber + ": " + target.getAbsolutePath());
    }

    private void handleDownload(String fileName, DataOutputStream out) throws IOException {
        File file = new File(currentDir, fileName);
        if (!file.exists() || file.isDirectory()) {
            out.writeUTF("File not found: " + fileName);
            return;
        }

        out.writeUTF("READY_FOR_DOWNLOAD");
        out.writeLong(file.length());

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        out.writeUTF("Download complete: " + fileName);
        System.out.println("File sent to client#" + clientNumber + ": " + file.getAbsolutePath());
    }
	
	public void sendStringToClient(String arg) {
		try {
			DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
			out.writeUTF(arg);
		} catch (IOException e) {
			System.out.println("Error handling command from client# " + clientNumber + ": " + e);
		}
	}

	public void run() {
		try {
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeUTF("Hello from server - you are client#" + clientNumber);
		}
		catch (IOException e) {
			System.out.println("Error handling client# " + clientNumber + ": " + e);
		}
	    finally {
	    	while(!this.exitSocket) {
	    		try {
	    			DataInputStream in = new DataInputStream(socket.getInputStream());
	                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
	    			
		    		String command;
		    		while((command = in.readUTF()) != null) {
		    			ExecuteCommand(command, in, out);
		    		}
		    		
	    		}
	    		catch (Exception e){
	    			System.out.println("Couldn't get input from socket");
	    		}

	    	}
	        try {
	            socket.close();
	        } catch (IOException e) {
	            System.out.println("Couldn't close a socket, what's going on?");
	        }
	        System.out.println("Connection with client# " + clientNumber+ " closed");
	    }
	}
}
