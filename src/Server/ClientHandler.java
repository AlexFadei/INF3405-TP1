package Server;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.LocalTime;
import LoggerUtil.LoggerUtil;

//worker thread for the client
public class ClientHandler extends Thread {
	private File currentFileDirectory;
	private Socket socket; private int clientNumber;
	private boolean exitSocket;
	
	public ClientHandler(Socket socket, int clientNumber) {
		this.socket = socket;
		this.clientNumber = clientNumber; System.out.println("New connection with client#" + clientNumber + " at" + socket);
		this.exitSocket = false;
		this.currentFileDirectory = new File(System.getProperty("user.dir"));
		
	}
	public void ExecuteCommand(String command) {
		String[] parts = command.split(" ", 2);
		String cmd = parts[0];
		String arg = parts.length > 1 ? parts[1] : "";
		LoggerUtil.log(this.socket, command);
		
		switch(cmd) {
				
		case "ls": handleLs(); break;
			
		case "cd": handleCd(arg); break;
			
		case "mkdir": handleMkDir(arg); break;
			
		case "upload": handleUpload(arg); break;
			
		case "download": handleDownload(arg); break;
			
		case "delete": handleDelete(arg); break;
		
		case "exit":
			
			this.exitSocket = true;
			break;
		default:
			this.sendStringToClient("Unrecognised Command, please try again");
			break;
		}
		
	}
	public boolean handleLs() {
		File[] listFiles = this.currentFileDirectory.listFiles();
		StringBuilder outputList = new StringBuilder("");
		if(listFiles == null) {
			return false;
		}
		for(File file : listFiles) {
			outputList.append(file.getName());
			outputList.append(file.isDirectory()? "/" : "");
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
		File requestedFileDirectory = new File(this.currentFileDirectory, arg).getAbsoluteFile();
		if( requestedFileDirectory.exists() &&  requestedFileDirectory.isDirectory()) {
			this.currentFileDirectory =  requestedFileDirectory;
		}
		this.sendStringToClient("changed directory to: " + arg);
		
		return true;};
	public boolean handleMkDir(String name) {
		if(name.isEmpty()) {
			this.sendStringToClient("invalid name");
			return false;
		}
		File createdFile = new File(this.currentFileDirectory, name);
	    if (createdFile.mkdir()) {
	    	this.sendStringToClient("Directory created: " + createdFile.getAbsolutePath());
	    } else {
	        this.sendStringToClient( "Failed to create directory: " + name);
	    }
		
		
		return true;};
	public boolean handleDelete(String name) {
		if(name.isEmpty()) {
			this.sendStringToClient("invalid name");
			return false;
		}
		File target = new File(this.currentFileDirectory, name);
		if(!target.exists()) {
			this.sendStringToClient( "File does not exist: " + name);
			return false;
		}
	    boolean success = target.isDirectory() ? target.delete() : target.delete();
	    if (success) {
	        this.sendStringToClient("Deleted: " + name);
	    } else {
	        this.sendStringToClient("Failed to delete: " + name);
	        return false;
	    }
		
		return true;};
	public boolean handleUpload(String arg) {return false;};
	public boolean handleDownload(String arg) {return false;};
	
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
	    			
		    		String command;
		    		while((command = in.readUTF()) != null) {
		    			ExecuteCommand(command);
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
