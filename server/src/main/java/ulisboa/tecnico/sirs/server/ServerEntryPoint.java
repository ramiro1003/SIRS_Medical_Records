package ulisboa.tecnico.sirs.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;

import javax.net.ssl.SSLServerSocketFactory;

import ulisboa.tecnico.sirs.domain.User;
import ulisboa.tecnico.sirs.library.domain.*;
import ulisboa.tecnico.sirs.server.database.DBGateway;
import ulisboa.tecnico.sirs.server.pep.PolicyEnforcementPoint;

public class ServerEntryPoint 
{
	private static DBGateway gateway = null;
	private static final String KEYSTORE_PATH = "resources/sirs.keyStore";
	private static int port;
	
	public static void main( String[] args )
	{
		gateway = new DBGateway();
		
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
	private User currUser;
	private DBGateway gateway;
	private PolicyEnforcementPoint pep;
	//private LoggingManager logMan;

	public ServerThread(Socket clientSocket, DBGateway gateway) throws IOException {
		this.socket = clientSocket;
		this.gateway = gateway;
		
		//this.logMan = new LoggingManager();
	}

	public void run() {

		try {
			// instantiate pep for thread
			this.pep = new PolicyEnforcementPoint();
			
			outStream = new ObjectOutputStream(socket.getOutputStream());
			inStream = new ObjectInputStream(socket.getInputStream());
			String cmd = null;
			Boolean quit = false;
			try {
				while(!quit) {
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
							quit = true;
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
		String password = (String) inStream.readObject();
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
			try {
				// Hash password + salt(email)
				MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
				String saltedPass = password + email;
				byte[] hashedPassBytes = sha256.digest(saltedPass.getBytes());
				String hashedPass = new String(hashedPassBytes);
				if(hashedPass.equals(gateway.getHashedPassword(email))) {
					outStream.writeObject(createUserView());
				}
				else {
					outStream.writeObject(null);
				}
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			outStream.writeObject("Bad login");
		}			
	}

	private boolean checkPasswordStrength(String password) {
		if(password.length() < 12) {
			return false;
		}
		else if(!password.matches(".*\\d.*")) {
			return false;
		}
		else {
			return true;
		}
	}
	
	private UserView createUserView() {
		String type = currUser.getType();
		UserView userView = null;
		// Create UserView based on user type/role
		switch(type) {
			case "Patient":
				userView = new PatientView(currUser.getUserId(), currUser.getEmail(), currUser.getName());
				break;
			case "Staff":
				userView = new StaffView(currUser.getUserId(), currUser.getEmail(), currUser.getName());
				break;
			case "Doctor":
				userView = new DoctorView(currUser.getUserId(), currUser.getEmail(), currUser.getName());
				break;
		}
		
		return userView;
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
		try {
			String patientId = (String) inStream.readObject();
			Boolean authorize = pep.enforce(currUser, patientId, "read");
			
			if (authorize) {
				// get the resource
				gateway.getRecord(patientId);
				System.out.println("Authorized!");
			}
			
			
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			String password = (String) inStream.readObject();
			// Check passsword strength
			if(checkPasswordStrength(password)) {
				// Hash password + salt(email)
				try {
					outStream.writeObject("Strong password");
					MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
					String saltedPass = password + email;
					byte[] hashedPassBytes = sha256.digest(saltedPass.getBytes());
					String hashedPass = new String(hashedPassBytes);
					//logMan.writeLog(hashedPass, currUser);
					gateway.registerUser(userId, email, name, type, hashedPass);
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				outStream.writeObject("Weak password");
			}
		} 
		else {
			outStream.writeObject("User Already Exists");
		}
	}


}
