package ulisboa.tecnico.sirs.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLServerSocketFactory;

import ulisboa.tecnico.sirs.domain.MedicalRecord;
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
		if(args.length == 2) {
			gateway.runTestScript(args[1]);
		}
		
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
			//start tls
			System.setProperty("javax.net.ssl.keyStore", KEYSTORE_PATH);
			System.setProperty("javax.net.ssl.keyStorePassword", "sirssirs");
			ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
			socket = ssf.createServerSocket(port);
			
			while(true) {
				try {
					//receive clients
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
						case "-changePassword":
							changePassword();
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
						case "-writeMD":
							writeMD();
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
	
	private void changePassword() {
		try {
			String password = (String) inStream.readObject();
			String email = currUser.getEmail();
			String username = currUser.getName();
			// Hash password + salt(email)
			MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
			String saltedPass = password + email;
			byte[] hashedPassBytes = sha256.digest(saltedPass.getBytes());
			String hashedPass = new String(hashedPassBytes);
			// Authenticate user
			if(hashedPass.equals(gateway.getHashedPassword(email))) {
				outStream.writeObject("User authenticated");
				// Check if user successfully matched passwords (so server doesn't wait on the new password for no reason)
				if(inStream.readObject().equals("Password confirmed")) {
					String newPass = (String) inStream.readObject();
					// Checks password strength
					if(checkPasswordStrength(newPass, username, email)) {
						outStream.writeObject("Strong password");
						gateway.updateUserPassword(email, newPass);
					}
					else {
						outStream.writeObject("Weak password");
					}
				}
			}
			else {
				outStream.writeObject("Wrong password");
			}
		} catch (ClassNotFoundException | NoSuchAlgorithmException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	private boolean checkPasswordStrength(String password, String name, String email) {
		// Get lower case password so we can make easier comparisons
		String lowerCasePass = password.toLowerCase();
		// Check if password contains at least one name of full user name. If so, password is considered weak
		String[] nameSplit = name.split(" ");
		for(String word : nameSplit) {
			if(lowerCasePass.contains(word.toLowerCase())) {
				return false;
			}
		}
		// Check if password contains at least one word of user email. If so, password is considered weak
		String[] emailSplit = email.split("[._@]");
		emailSplit = Arrays.copyOf(emailSplit, emailSplit.length - 1);
		for(String word : emailSplit) {
			if(lowerCasePass.contains(word.toLowerCase())) {
				return false;
			}
		}
		// Check if password as less than 12 characters. If so, password is considered weak
		if(password.length() < 12) {
			return false;
		}
		// Check if password does not have at least 1 number, 1 upper case letter and 1 lower case letter. If so, password is considered weak
		else if(!password.matches("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])[a-zA-Z\\d]{12,}$")) { //FIXME Regex sacado
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
		//logMan.writeLog(user, currUser);
		System.out.println(currUser.getType() + " " + currUser.getName() + " (" + currUser.getEmail() + ") disconnected.");
	}
	
	private void readMD() {
		try {
			String patientId = (String) inStream.readObject();
			MedicalRecord medicalRecord = gateway.getMedicalRecord(patientId);
			
			Boolean authorize = pep.enforce(currUser, medicalRecord, "read", "context");
			
			if (authorize) {
				// return MedicalRecordView
			}
			else {
				
			}			
			
			
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void writeMD() {
		try {
			String patientId = (String) inStream.readObject();
			MedicalRecord medicalRecord = gateway.getMedicalRecord(patientId);
			
			Boolean authorize = pep.enforce(currUser, medicalRecord, "write", "context");
			
			if (authorize) {
				
			}
			
			
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void registerUser() throws ClassNotFoundException, IOException {
		// Checks if user already exists
		int userId = Integer.parseInt((String) inStream.readObject());
		//logMan.writeLog(email, currUser);
		List<User> users = gateway.getUsers(); 
		boolean found = false;
		for(User user : users) {
			if(user.getUserId() == userId){
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
			if(checkPasswordStrength(password, name, email)) {
				// Hash password + salt(email)
				try {
					outStream.writeObject("Strong password");
					MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
					String saltedPass = password + email;
					byte[] hashedPassBytes = sha256.digest(saltedPass.getBytes());
					String hashedPass = new String(hashedPassBytes);
					//logMan.writeLog(hashedPass, currUser);
					String birthDate = null;
					gateway.registerUser(userId, email, name, type, hashedPass, birthDate);
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
