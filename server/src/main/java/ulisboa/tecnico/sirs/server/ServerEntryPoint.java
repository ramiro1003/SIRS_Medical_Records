package ulisboa.tecnico.sirs.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
		String[] input = args[0].split(":");
		gateway = new DBGateway();
		if(input.length == 2) {
			gateway.runTestScript(input[1]);
		}
		
		SSLServerSocketFactory ssf;
		ServerSocket socket = null;
		// Check if port is valid
		try {
			port = Integer.parseInt(input[0]);
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
						case "-listP":
							listDoctorPatients();
							break;
						case "-loginUser":
							loginUser();
							break;
						case "-readMD":
							readMD();
							break;
						case "-registerUser":
							registerUser();
							break;
						case "-writeMD":
							writeMD();
							break;
						case "-quit":
							quitUser();
							quit = true;
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
			Integer userId = currUser.getUserId();
			String username = currUser.getName();
			// Hash password + salt(userId)
			MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
			String saltedPass = password + userId;
			byte[] hashedPassBytes = sha256.digest(saltedPass.getBytes());
			String hashedPass = new String(hashedPassBytes);
			// Authenticate user
			if(hashedPass.equals(gateway.getHashedPassword(userId))) {
				outStream.writeObject("User authenticated");
				// Check if user successfully matched passwords (so server doesn't wait on the new password for no reason)
				if(inStream.readObject().equals("Password confirmed")) {
					String newPass = (String) inStream.readObject();
					// Checks password strength
					if(checkPasswordStrength(newPass, username)) {
						outStream.writeObject("Strong password");
						gateway.updateUserPassword(userId, newPass);
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
	
	private boolean checkPasswordStrength(String password, String name) {
		// Get lower case password so we can make easier comparisons
		String lowerCasePass = password.toLowerCase();
		// Check if password contains at least one name of full user name. If so, password is considered weak
		String[] nameSplit = name.split(" ");
		for(String word : nameSplit) {
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
	
	private void listDoctorPatients() {
		Integer doctorId = currUser.getUserId();
		List<PatientView> patients = gateway.getDoctorPatients(doctorId.toString());
		try {
			if(patients.isEmpty()) {
				outStream.writeObject("Doctor has no patients");
			}
			else {
				outStream.writeObject("Doctor has patients");
				outStream.writeObject(patients);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void loginUser() throws ClassNotFoundException, IOException {
		Integer userId = (Integer) inStream.readObject();
		//logMan.writeLog(userId, currUser);
		String password = (String) inStream.readObject();
		//logMan.writeLog(hashedPass, currUser);
		// First checks if user exists
		List<User> users = gateway.getUsers(); 
		boolean found = false;
		for(User user : users) {
			if(user.getUserId() == userId) {
				found = true;
				currUser = user;
				break;
			}
		}
		// If user exists, checks if hashed password matches with the one stored on the database
		if(found) {
			try {
				// Hash password + salt(userId)
				MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
				String saltedPass = password + userId;
				byte[] hashedPassBytes = sha256.digest(saltedPass.getBytes());
				String hashedPass = new String(hashedPassBytes);
				if(hashedPass.equals(gateway.getHashedPassword(userId))) {
					outStream.writeObject("User logged in");
					outStream.writeObject(createUserView());
				}
				else {
					outStream.writeObject("Bad login");
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
	
	private UserView createUserView() {
		String type = currUser.getType();
		UserView userView = null;
		// Create UserView based on user type/role
		switch(type) {
			case "Patient":
				userView = new PatientView(currUser.getUserId(), currUser.getName());
				break;
			case "Staff":
				userView = new StaffView(currUser.getUserId(), currUser.getName());
				break;
			case "Doctor":
				userView = new DoctorView(currUser.getUserId(), currUser.getName());
				break;
		}
		
		return userView;
	}
	
	private MedicalRecordView createMedicalRecordView(MedicalRecord medicalRecord) {		
		Integer recordId = medicalRecord.getRecordId();
		Integer patientId = medicalRecord.getPatientId();
		Integer doctorId = medicalRecord.getDoctorId();
		String name = medicalRecord.getName();
		Integer height = medicalRecord.getHeight();
		Integer weight = medicalRecord.getWeight();
		List<List<String>> prescriptionsInfo = medicalRecord.getPrescriptionsInfo();
		List<List<String>> diagnosesInfo = medicalRecord.getDiagnosesInfo();
		List<List<String>> treatmentsInfo = medicalRecord.getTreatmentsInfo();
		
		return new MedicalRecordView(recordId, patientId, doctorId, name, height, weight, prescriptionsInfo, diagnosesInfo, treatmentsInfo);		
	}
	
	private void readMD() {
		try {
			String patientId = (String) inStream.readObject();
			MedicalRecord medicalRecord = gateway.getMedicalRecord(patientId);
			Boolean authorize = pep.enforce(currUser, medicalRecord, "read", "context");
			if (authorize) {
				outStream.writeObject("Authorized");
				outStream.writeObject(createMedicalRecordView(medicalRecord));
			}
			else {
				outStream.writeObject("Non authorized");
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
			Boolean authorize = pep.enforce(currUser, medicalRecord, "write", "context"); // FIXME CONTEXT SENT
			
			if (authorize) {
				outStream.writeObject("Authorized");
				
				Integer patientIdInt = Integer.parseInt(patientId);
				
				String writeCmd = null;
				Boolean writeQuit = false;
				try {
					while(!writeQuit) {
						writeCmd = (String) inStream.readObject();
						//logMan.writeLog(writeCmd, currUser);
						switch(writeCmd) {
							case "-changeHeight":
								changeHeight(patientIdInt);
								break;
							case "-changeWeight":
								changeWeight(patientIdInt);
								break;
							case "-addPrescription":
								addPrescription(patientIdInt);
								break;
							case "-addDiagnosis":
								addDiagnosis(patientIdInt);
								break;
							case "-addTreatment":
								addTreatment(patientIdInt);
								break;
							case "-quitWrite":
								writeQuit = true;
								break;
						}
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
				
				
			}
			else {
				outStream.writeObject("Non authorized");
			}
			
			
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addTreatment(Integer patientId) {
		// TODO Auto-generated method stub
		
	}

	private void addDiagnosis(Integer patientId) {
		// TODO Auto-generated method stub
		
	}

	private void addPrescription(Integer patientId) {
		// TODO Auto-generated method stub
		
	}

	private void changeHeight(Integer patientId) {
		String height;
		try {
			height = (String) inStream.readObject();
			
			gateway.changeHeight(patientId, height);
			outStream.writeObject("complete");
			
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private void changeWeight(Integer patientId) {
		try {
			String weight = (String) inStream.readObject();
			
			gateway.changeWeight(patientId, weight);
			
			outStream.writeObject("complete");
			
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	

	private void registerUser() throws ClassNotFoundException, IOException {
		// Checks if user already exists
		Integer userId = Integer.parseInt((String) inStream.readObject());
		//logMan.writeLog(userId, currUser);
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
			String name = (String) inStream.readObject();
			//logMan.writeLog(name, currUser);
			String type = (String) inStream.readObject();
			//logMan.writeLog(type, currUser);
			String password = (String) inStream.readObject();
			// Check passsword strength
			if(checkPasswordStrength(password, name)) {
				// Hash password + salt(userId)
				try {
					outStream.writeObject("Strong password");
					MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
					String saltedPass = password + userId.toString();
					byte[] hashedPassBytes = sha256.digest(saltedPass.getBytes());
					String hashedPass = new String(hashedPassBytes);
					//logMan.writeLog(hashedPass, currUser);
					String birthDate = null;
					gateway.registerUser(userId, name, type, hashedPass, birthDate);
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
	
	private void quitUser() {
		//logMan.writeLog(user, currUser);
		System.out.println(currUser.getType() + " " + currUser.getName() + " disconnected.");
	}
}
