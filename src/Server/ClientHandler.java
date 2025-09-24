package Server;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.LocalTime;
import LoggerUtil.LoggerUtil;

//worker thread for the client
public class ClientHandler extends Thread {
	private Socket socket; private int clientNumber;
	private boolean exitSocket;
	
	public ClientHandler(Socket socket, int clientNumber) {
		this.socket = socket;
		this.clientNumber = clientNumber; System.out.println("New connection with client#" + clientNumber + " at" + socket);
		this.exitSocket = false;
	}
	public void ExecuteCommand(String command) {

		String[] parts = command.split(" ", 2);
		switch(parts[0]) {
				
		case "ls":
			LoggerUtil.log(this.socket, command);
			break;
			
		case "cd":
			LoggerUtil.log(this.socket, command);
			break;
			
		case "mkdir":
			LoggerUtil.log(this.socket, command);
			break;
			
		case "upload":
			LoggerUtil.log(this.socket, command);
			break;
			
		case "download":
			LoggerUtil.log(this.socket, command);
			break;
			
		case "delete":
			LoggerUtil.log(this.socket, command);
			break;
			
		case "exit":
			LoggerUtil.log(this.socket, command);
			this.exitSocket = true;
			break;
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
