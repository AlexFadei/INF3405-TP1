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
        System.out.println("Nouvelle connexion avec le client#" + clientNumber + " sur " + socket);
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
			this.sendStringToClient("Commande non reconnue: veuillez réessayer");
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
			this.sendStringToClient("dossier invalide");
			return false;
		}
		if (arg.equals("..")) {
	        if (currentDir.equals(rootDir)) {
	            this.sendStringToClient("Déjà au fichier racine");
	            return true;
	        }

	        currentDir = currentDir.getParentFile();
	        this.sendStringToClient("Changement de dossier vers : " + currentDir.getAbsolutePath());
	        return true;
	    }
		File requestedFileDirectory = new File(this.currentDir, arg).getAbsoluteFile();
		if( requestedFileDirectory.exists() &&  requestedFileDirectory.isDirectory()) {
			this.currentDir =  requestedFileDirectory;
			this.sendStringToClient("Changement de dossier vers : " + arg);
		}
		
		
		return true;};
	public boolean handleMkDir(String name) {
		if(name.isEmpty()) {
			this.sendStringToClient("nom invalide");
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
		
	    boolean isDirectory = target.isDirectory();
	    try {
	        deleteRecursively(target);

	        if (isDirectory) {
	            this.sendStringToClient("le dossier " + name + " a été supprimé");
	        } else {
	            this.sendStringToClient("le fichier " + name + " a été supprimé");
	        }

	        return true;
	    } catch (IOException e) {
	        this.sendStringToClient("Erreur lors de la suppression de " + name + " : " + e.getMessage());
	        return false;
	    }
	}
		
	private void deleteRecursively(File file) throws IOException {
	    if (file.isDirectory()) {
	        File[] entries = file.listFiles();
	        if (entries != null) {
	            for (File entry : entries) {
	                deleteRecursively(entry);
	            }
	        }
	    }
	    if (!file.delete()) {
	        throw new IOException("Impossible de supprimer : " + file.getAbsolutePath());
	    }
	}
		
	private void handleUpload(String fileName, DataInputStream in, DataOutputStream out) throws IOException {
        if (fileName.isEmpty()) {
            out.writeUTF("Paramètres invalides pour upload.");
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

        out.writeUTF("Upload réussi : " + fileName);
        System.out.println("Fichier upload par client#" + clientNumber + ": " + target.getAbsolutePath());
    }

	private void handleDownload(String fileName, DataOutputStream out) throws IOException {
	    if (fileName.isEmpty()) {
	        out.writeUTF("Paramètre invalides pour download.");
	        return;
	    }

	    File target = new File(currentDir, fileName);
	    if (!target.exists() || !target.isFile()) {
	        out.writeUTF("ERROR: Fichier introuvable sur le serveur.");
	        return;
	    }

	    out.writeUTF("READY_FOR_DOWNLOAD");
	    out.writeLong(target.length());

	    try (FileInputStream fis = new FileInputStream(target)) {
	        byte[] buffer = new byte[4096];
	        int bytesRead;
	        while ((bytesRead = fis.read(buffer)) != -1) {
	            out.write(buffer, 0, bytesRead);
	        }
	        out.flush();
	    }

	    out.writeUTF("Download réussi : " + fileName);
	    System.out.println("Fichier envoyé à client#" + clientNumber + ": " + target.getAbsolutePath());
	}
	
	public void sendStringToClient(String arg) {
		try {
			DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
			out.writeUTF(arg);
		} catch (IOException e) {
			System.out.println("Erreur lors du traitement de la commande du client# " + clientNumber + ": " + e);
		}
	}

	public void run() {
		try {
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeUTF("Bonjour du serveur - vous êtes le client#" + clientNumber);
		}
		catch (IOException e) {
			System.out.println("Erreur lors de la gestion du client# " + clientNumber + ": " + e);
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
	    			System.out.println("Impossible d’obtenir les données depuis le socket");
	    		}

	    	}
	        try {
	            socket.close();
	        } catch (IOException e) {
	            System.out.println("Impossible de fermer le socket, que se passe-t-il ?");
	        }
	        System.out.println("Connection avec le client# " + clientNumber+ " fermée");
	    }
	}
}
