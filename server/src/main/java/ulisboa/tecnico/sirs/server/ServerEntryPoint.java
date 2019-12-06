package ulisboa.tecnico.sirs.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;

import javax.net.ssl.SSLServerSocketFactory;

import ulisboa.tecnico.sirs.domain.User;
import ulisboa.tecnico.sirs.server.database.DBGateway;
import ulisboa.tecnico.sirs.server.pep.PolicyEnforcementPoint;

public class ServerEntryPoint 
{
	private static DBGateway gateway = null;
	private static PolicyEnforcementPoint pep = null;
	private static final String KEYSTORE_PATH = "sirs.keyStore";
	private static int port;
	
	public static void main( String[] args )
	{
		
		gateway = new DBGateway();
		try {
			pep = new PolicyEnforcementPoint();
		} catch (IllegalArgumentException | IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		//receive clients
		SSLServerSocketFactory ssf;
		ServerSocket socket = null;
		// Check if port is valid
		try {
			port = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			System.out.println("Port must be an integer");
			System.exit(0);
		}
		
		try {
			System.setProperty("javax.net.ssl.keyStore", KEYSTORE_PATH);
			System.setProperty("javax.net.ssl.keyStorePassword", "sirssirs");
			ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
			socket = ssf.createServerSocket(port);
			
			
			while(true) {
				try {
					Socket inSocket = socket.accept();
					ServerThread newServerThread = new ServerThread(inSocket, gateway, pep);
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
	private User currUser;
	private DBGateway gateway;
	private PolicyEnforcementPoint pep;
	//private LoggingManager logMan;

	public ServerThread(Socket clientSocket, DBGateway gateway, PolicyEnforcementPoint pep) throws IOException {
		this.socket = clientSocket;
		this.gateway = gateway;
		this.pep = pep;
		//this.logMan = new LoggingManager();
	}

	public void run() {

		try {
			outStream = new ObjectOutputStream(socket.getOutputStream());
			inStream = new ObjectInputStream(socket.getInputStream());

			String cmd = null;
			try {
				while(true) {
					cmd = (String) inStream.readObject();
					//logMan.writeLog(cmd, currUser);
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
		//logMan.writeLog(email, currUser);
		String hashedPass = (String) inStream.readObject();
		//logMan.writeLog(hashedPass, currUser);
		// First checks if user exists
		List<User> users = gateway.getUsers(); 
		boolean found = false;
		for(User user : users) {
			if(user.getEmail().equals(email)) {
				found = true;
				currUser = user;
				break;
			}
		}
		// If user exists, checks if hashed password matches with the one stored on the database
		if(found) {
			if(hashedPass.equals(gateway.getHashedPassword(email))) {
				outStream.writeObject("Successful login");
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
			//logMan.writeLog(user, currUser);
			System.out.println("User " + user + " disconnected.");
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	private void readMD() {
		// Get patient Id
		String patientId = (String) inStream.readObject();
		pep.authorize("read", currUser.getUserId(), currUser.getRole(), patientId, false);

	}

	private void registerUser() throws ClassNotFoundException, IOException {
		// Checks if user already exists
		String userId = (String) inStream.readObject();
		//logMan.writeLog(email, currUser);
		List<User> users = gateway.getUsers(); 
		boolean found = false;
		for(User user : users) {
			if(user.getUserId().equals(userId)) {
				found = true;
				break;
			}
		}
		// Creates a new user
		if(!found) {
			outStream.writeObject("New user");
			String email = (String) inStream.readObject();
			String name = (String) inStream.readObject();
			//logMan.writeLog(name, currUser);
			String type = (String) inStream.readObject();
			//logMan.writeLog(type, currUser);
			String hashedPass = (String) inStream.readObject();
			//logMan.writeLog(hashedPass, currUser);
			gateway.registerUser(userId, email, name, type, hashedPass);
		} 
		else {
			outStream.writeObject("User Already Exists");
		}
	}


}
