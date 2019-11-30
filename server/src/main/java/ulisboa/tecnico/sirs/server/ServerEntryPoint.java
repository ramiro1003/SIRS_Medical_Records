package ulisboa.tecnico.sirs.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;

import ulisboa.tecnico.sirs.domain.User;
import ulisboa.tecnico.sirs.server.database.DBGateway;

public class ServerEntryPoint 
{
		
	public static void main( String[] args )
	{
		
		DBGateway gateway = new DBGateway();

		//receive clients
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(Integer.parseInt(args[0]));
			while(true) {
				try {
					Socket inSocket = serverSocket.accept();
					ServerThread newServerThread = new ServerThread(inSocket, gateway);
					newServerThread.start();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				gateway.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}

}


// Class that is instantiated when a user connects to the server
class ServerThread extends Thread {
	
	private Socket socket = null;
	ObjectOutputStream outStream;
	ObjectInputStream inStream;
	private String currUser;
	private DBGateway gateway;

	public ServerThread(Socket clientSocket, DBGateway gateway) {
		socket = clientSocket;
		this.gateway = gateway;
	}

	public void run() {

		try {
			outStream = new ObjectOutputStream(socket.getOutputStream());
			inStream = new ObjectInputStream(socket.getInputStream());

			String cmd = null;

			try {
				while(true) {
					cmd = (String) inStream.readObject();
					switch(cmd) {
						case "-listMD":
							listMD();
							break;
						case "-loginUser":
							loginUser();
							break;
						case "-quit":
							quitUser();
							break;
						case "-readMD":
							readMD();
							break;
						case "-registerUser":
							registerUser();
							break;
						}
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void listMD() {
		// TODO Auto-generated method stub

	}

	private void loginUser() throws ClassNotFoundException, IOException {
		String email = (String) inStream.readObject();
		String hashedPass = (String) inStream.readObject();
		// First checks if user exists
		List<User> users = gateway.getUsers(); 
		boolean found = false;
		for(User user : users) {
			if(user.getEmail().equals(email)) {
				found = true;
				break;
			}
		}
		// If user exists, checks if hashed password matches with the one stored on the database
		if(found) {
			if(hashedPass.equals(gateway.getHashedPassword(email))) {
				outStream.writeObject("Successful login");
				currUser = email;
			}
			else {
				outStream.writeObject("Bad login");
			}
		}
		else {
			outStream.writeObject("Bad login");
		}			
	}
	
	private void quitUser() {
		try {
			String user = (String) inStream.readObject();
			System.out.println("User " + user + " disconnected.");
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	private void readMD() {
		// TODO Auto-generated method stub

	}

	private void registerUser() throws ClassNotFoundException, IOException {
		// First checks if user already exists
		String email = (String) inStream.readObject();
		List<User> users = gateway.getUsers(); 
		boolean found = false;
		for(User user : users) {
			if(user.getName().equals(email)) {
				found = true;
				break;
			}
		}
		// If user does not exist, creates a new user
		if(!found) {
			outStream.writeObject("New email");
			String name = (String) inStream.readObject();
			String type = (String) inStream.readObject();
			String hashedPass = (String) inStream.readObject();
			gateway.registerUser(email, name, type, hashedPass);
		} 
		else {
			outStream.writeObject("User Already Exists");
		}
	}


}
