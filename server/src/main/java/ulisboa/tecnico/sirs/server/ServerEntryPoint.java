package ulisboa.tecnico.sirs.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.net.ssl.SSLServerSocketFactory;

import ulisboa.tecnico.sirs.domain.MedicalRecord;
import ulisboa.tecnico.sirs.domain.User;
import ulisboa.tecnico.sirs.library.domain.*;
import ulisboa.tecnico.sirs.server.database.DBGateway;
import ulisboa.tecnico.sirs.server.logging.LoggingManager;
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
			
			String keystorePwd = getKeystoreCredentials();

			System.setProperty("javax.net.ssl.keyStore", KEYSTORE_PATH);
			System.setProperty("javax.net.ssl.keyStorePassword", keystorePwd);
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


	
	private static String getKeystoreCredentials() {
		// Read server keystore credentials
		String path = "resources/serverkeystore.txt";
		String keystorePwd = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			String ksLine = reader.readLine();
			reader.close();
			
			keystorePwd = ksLine.split("=")[1];
			
		} catch (IOException e) {
			System.out.println("Exception: Could not read keystore credentials.");
			e.printStackTrace();
		}
		return keystorePwd;
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
	private String context;
	//private LoggingManager logMan;

	public ServerThread(Socket clientSocket, DBGateway gateway) throws IOException {
		this.socket = clientSocket;
		this.gateway = gateway;
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
					if(!cmd.equals("-loginUser")) {
						LoggingManager.writeLog(cmd, currUser.getUserId().toString());
					}
					switch(cmd) {
						case "-changePassword":
							changePassword();
							break;
						case "-listP":
							listDoctorPatients();
							break;
						case "-loginUser":
							LoggingManager.writeLog(cmd, "");
							loginUser();
							break;
						case "-readMD":
							readMD();
							break;
						case "-createMD":
							createMD();
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
	
	private void readMD() {
		try {
			String patientId = (String) inStream.readObject();
			LoggingManager.writeLog("Read medical record: " + patientId, 
					currUser.getUserId().toString());
			MedicalRecord medicalRecord = gateway.getMedicalRecord(patientId);
			
			if(medicalRecord ==  null) {
				outStream.writeObject("Not authorized");
			}
			else {
				Boolean authorize = pep.enforce(currUser, medicalRecord, "read", context);
				if (authorize) {
					outStream.writeObject("Authorized");
					outStream.writeObject(createMedicalRecordView(medicalRecord));
				}
				else {
					outStream.writeObject("Not authorized");
				}
			}
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void createMD() {
		try {
			String patientId = (String) inStream.readObject();
			LoggingManager.writeLog("Read medical record: " + patientId, currUser.getUserId().toString());
			LoggingManager.writeLog("Creating medical record for " + patientId, currUser.getUserId().toString());
			String patientName = gateway.getPatientName(Integer.parseInt(patientId));
			System.out.print(patientName);
			// Check if patient exists
			if(patientName == null) {
				outStream.writeObject("Patient does not exist");
			}
			else {
				// Check if medical record already exists
				MedicalRecord record = gateway.getMedicalRecord(patientId);
				if(record != null) {
					outStream.writeObject("Patient already assigned");
				}
				// Create medical record
				else {
					outStream.writeObject("Create medical record");
					String height = (String) inStream.readObject();
					String weight = (String) inStream.readObject();
					Integer numberOfRecords = gateway.getAllMedicalRecordsNum();
					gateway.addMedicalRecord(numberOfRecords + 1, Integer.parseInt(patientId), currUser.getUserId(), weight, height);
				}
			}
		} catch (NumberFormatException | ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
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
			String saltedPass = password + userId.toString();
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
						String saltedNewPass = newPass + userId.toString();
						byte[] hashedNewPassBytes = sha256.digest(saltedNewPass.getBytes());
						String hashedNewPass = new String(hashedNewPassBytes);
						gateway.updateUserPassword(userId, hashedNewPass);
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
		// Get userId
		Integer userId = (Integer) inStream.readObject();
		LoggingManager.writeLog("Logging: " + userId.toString(),"");
		// Get password
		String password = (String) inStream.readObject();
		// Get context
		context = (String) inStream.readObject();
		LoggingManager.writeLog("Context of log: " + context,"");
		// First checks if user exists
		List<User> users = gateway.getUsers(); 
		boolean found = false;
		for(User user : users) {
			if(user.getUserId().equals(userId)) {
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
				String saltedPass = password + userId.toString();
				byte[] hashedPassBytes = sha256.digest(saltedPass.getBytes());
				String hashedPass = new String(hashedPassBytes);
				if(hashedPass.equals(gateway.getHashedPassword(userId))) {
					outStream.writeObject("User logged in");
					outStream.writeObject(createUserView());
					if(currUser.getType().equals("Patient") || currUser.getType().equals("Secretary")) {
						if(context.equals("operationroom") || context.equals("emergency")) {
							LoggingManager.writeLog("-loginUser !!!!!! ALERT !!!!! Patient logging from an " + context + " machine", currUser.getUserId().toString());
						}
						else {
							LoggingManager.writeLog("-loginUser", currUser.getUserId().toString());
						}
					}
					else {
						LoggingManager.writeLog("-loginUser", currUser.getUserId().toString());
					}			
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
			case "Secretary":
				userView = new SecretaryView(currUser.getUserId(), currUser.getName());
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
	
	private void writeMD() {
		try {
			String patientId = (String) inStream.readObject();
			MedicalRecord medicalRecord = gateway.getMedicalRecord(patientId);
			
			if(medicalRecord ==  null) {
				outStream.writeObject("Not authorized");
			}
			else {
				Boolean authorize = pep.enforce(currUser, medicalRecord, "write", context);
				
				if (authorize) {
					outStream.writeObject("Authorized");
					
					Integer patientIdInt = Integer.parseInt(patientId);
					
					String writeCmd = null;
					Boolean writeQuit = false;
					try {
						while(!writeQuit) {
							writeCmd = (String) inStream.readObject();
							LoggingManager.writeLog(writeCmd,
									currUser.getUserId().toString());
							switch(writeCmd) {
								case "-changeHeight":
									changeHeight(patientIdInt);
									break;
								case "-changeWeight":
									changeWeight(patientIdInt);
									break;
								case "-addPrescription":
									addPrescription(patientIdInt, medicalRecord.getRecordId());
									break;
								case "-addDiagnosis":
									addDiagnosis(patientIdInt, medicalRecord.getRecordId());
									break;
								case "-addTreatment":
									addTreatment(patientIdInt, medicalRecord.getRecordId());
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
					outStream.writeObject("Not authorized");
				}
				
			}
			
			
			
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	


	private void changeHeight(Integer patientId) {
		String height;
		try {
			height = (String) inStream.readObject();
			LoggingManager.writeLog("Change height: " + height, 
					currUser.getUserId().toString());
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
			LoggingManager.writeLog("Change height: " + weight, 
					currUser.getUserId().toString());
			gateway.changeWeight(patientId, weight);
			
			outStream.writeObject("complete");
			
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private void addPrescription(Integer patientId, Integer medicalRecordId) {
		try {
			String prescriptionName = (String) inStream.readObject();
			LoggingManager.writeLog("Add prescription name: " + prescriptionName, 
					currUser.getUserId().toString());
			DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;
			String prescriptionDate = formatter.format(LocalDate.now());
			
			// note: method names differ, but prescription == medication
			gateway.addPrescription(medicalRecordId, prescriptionName, prescriptionDate);
			
			outStream.writeObject("complete");
			
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void addDiagnosis(Integer patientId, Integer medicalRecordId) {
		try {
			String diagnosis = (String) inStream.readObject();
			LoggingManager.writeLog("Add diagnosis name: " + diagnosis, 
					currUser.getUserId().toString());
			String diagnosisDescription = (String) inStream.readObject();
			LoggingManager.writeLog("Add diagnosis description: " + diagnosisDescription, 
					currUser.getUserId().toString());
			DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;
			String diagnosisDate = formatter.format(LocalDate.now());
			
			gateway.addDiagnosis(medicalRecordId, diagnosis, diagnosisDate, diagnosisDescription);
			
			outStream.writeObject("complete");
			
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void addTreatment(Integer patientId, Integer medicalRecordId) {
		try {
			String treatment = (String) inStream.readObject();
			LoggingManager.writeLog("Add treatment name: " + treatment, 
					currUser.getUserId().toString());
			String treatmentDescription = (String) inStream.readObject();
			LoggingManager.writeLog("Add treatment description: " + treatmentDescription, 
					currUser.getUserId().toString());
			DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;
			String treatmentDate = formatter.format(LocalDate.now());
			
			gateway.addTreatment(medicalRecordId, treatment, treatmentDate, treatmentDescription);
			
			outStream.writeObject("complete");
			
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	

	private void registerUser() throws ClassNotFoundException, IOException {
		// Checks if user already exists
		Integer userId = Integer.parseInt((String) inStream.readObject());
		LoggingManager.writeLog("Register user: " + userId,
				currUser.getUserId().toString());
		List<User> users = gateway.getUsers(); 
		boolean found = false;
		for(User user : users) {
			if(user.getUserId().equals(userId)){
				found = true;
				break;
			}
		}
		// Creates a new user
		if(!found) {
			outStream.writeObject("New user");
			String name = (String) inStream.readObject();
			LoggingManager.writeLog("Create user: " + name, 
					currUser.getUserId().toString());
			String type = (String) inStream.readObject();
			LoggingManager.writeLog("Create type: " + type, 
					currUser.getUserId().toString());
			String birthDate = (String) inStream.readObject();
			LoggingManager.writeLog("Create birthdate: " + birthDate, 
					currUser.getUserId().toString());
			String password = (String) inStream.readObject();
			LoggingManager.writeLog("Create password: " + password, 
					currUser.getUserId().toString());
			// Hash password + salt(userId)
			try {
				MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
				String saltedPass = password + userId.toString();
				byte[] hashedPassBytes = sha256.digest(saltedPass.getBytes());
				String hashedPass = new String(hashedPassBytes);
				gateway.registerUser(userId, name, type, birthDate, hashedPass);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		} 
		else {
			outStream.writeObject("User Already Exists");
		}
	}
	
	private void quitUser() {
		try {
			LoggingManager.writeLog("Quit", 
					currUser.getUserId().toString());
		} catch (IOException e) {
			e.printStackTrace();
		}		
		System.out.println(currUser.getType() + " " + currUser.getName() + " disconnected.");
	}
}
