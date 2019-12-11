package ulisboa.tecnico.sirs.server.database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ulisboa.tecnico.sirs.crypto.CriptographyManager;
import ulisboa.tecnico.sirs.domain.*;
import ulisboa.tecnico.sirs.library.domain.PatientView;

public class DBGateway {

	private static final String SQL_SELECT_USER = "SELECT * FROM User";
	private static final String SQL_INSERT_USER = "INSERT INTO User (Id, Name, Type, BirthDate) VALUES (?, ?, ?, ?)";
	private static final String SQL_SELECT_AUTH = "SELECT HashedPass FROM Auth WHERE UserId=?";
	private static final String SQL_INSERT_AUTH = "INSERT INTO Auth (UserId, HashedPass) VALUES (?, ?)";
	private static final String SQL_UPDATE_AUTH = "UPDATE Auth SET HashedPass=? WHERE UserId=?";
	private static final String SQL_GET_MEDICAL_RECORD = "SELECT * FROM MedicalRecord INNER JOIN User ON MedicalRecord.PatientId = User.Id WHERE MedicalRecord.PatientId = ?";
	private static final String SQL_GET_ALL_MEDICAL_RECORD = "SELECT * FROM MedicalRecord";
	private static final String SQL_INSERT_MEDICAL_RECORD = "INSERT INTO MedicalRecord (Id, PatientId, DoctorId, Weight, Height) VALUES (?,?,?,?,?)";
	private static final String SQL_CHANGE_WEIGHT = "UPDATE MedicalRecord SET Weight=? WHERE PatientId = ?";
	private static final String SQL_CHANGE_HEIGHT = "UPDATE MedicalRecord SET Height=? WHERE PatientId = ?";
	private static final String SQL_INSERT_MEDICATION = "INSERT INTO Medication (MedicalRecordId, Name, PrescriptionDate) VALUES (?,?,?)";
	private static final String SQL_INSERT_TREATMENT = "INSERT INTO Treatment (MedicalRecordId, Name, TreatmentDate, Description) VALUES (?,?,?,?)";
	private static final String SQL_INSERT_DIAGNOSIS = "INSERT INTO Diagnosis (MedicalRecordId, Name, DiagnosisDate, Description) VALUES (?,?,?,?)";
	private static final String SQL_GET_MEDICATION = "SELECT * FROM Medication WHERE MedicalRecordId=?";
	private static final String SQL_GET_TREATMENT = "SELECT * FROM Treatment WHERE MedicalRecordId=?";
	private static final String SQL_GET_DIAGNOSIS = "SELECT * FROM Diagnosis WHERE MedicalRecordId=?";
	private static final String SQL_GET_DOCTOR_PATIENTS = "SELECT PatientId, Name FROM User INNER JOIN MedicalRecord WHERE User.Id=MedicalRecord.PatientId and DoctorId=?";
	private static final String SQL_GET_USER_NAME = "SELECT Name FROM User Where Id=?";

	private String db;
	private String password;
	
	private CriptographyManager cManager;
	private DBManager manager;

	public DBGateway() {
		
		getDatabaseCredentials();
		
		cManager = new CriptographyManager();
		
		//Connection to database
		manager = new DBManager();
		manager.setProperties("jdbc:mariadb://localhost:3306/sirs", db, password);

		try {
			manager.createDefaultDB();
		} catch (IOException | SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}
	
	public void getDatabaseCredentials() {
		// Read local database credentials
		String path = "resources/dbconfig.txt";
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			String dbName = reader.readLine();
			String dbPwd = reader.readLine();
			reader.close();
			
			this.db = dbName.split("=")[1];
			this.password = dbPwd.split("=")[1];
			
		} catch (IOException e) {
			System.out.println("Exception: Could not read database credentials.");
			e.printStackTrace();
		}
		
		
	}
	
	public void registerUser(Integer userId, String name, String type, String birthDate, String hashedPass) {
		try{
			// Insert into User table
			PreparedStatement stmt = this.manager.getConnection().prepareStatement(SQL_INSERT_USER);
			stmt.setInt(1, userId);
			stmt.setString(2, cManager.cipher(name));
			stmt.setString(3, cManager.cipher(type));
			stmt.setString(4, cManager.cipher(birthDate));
			stmt.executeUpdate();
			// Insert into Auth table
			PreparedStatement stmt2 = this.manager.getConnection().prepareStatement(SQL_INSERT_AUTH);
			stmt2.setInt(1, userId);
			stmt2.setString(2, hashedPass);
			stmt2.executeUpdate();
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		} catch (Exception e) {
			System.out.println("Problems ciphering!");
		}
	}
	
	public void updateUserPassword(Integer userId, String newPass) {
		try{
			// Update Auth table
			PreparedStatement stmt = this.manager.getConnection().prepareStatement(SQL_UPDATE_AUTH);
			stmt.setString(1, newPass);
			stmt.setInt(2, userId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		}
	}
	
	public void addMedicalRecord(Integer id, Integer patientId, Integer doctorId, String weight, String height) {
		
		//cipher strings
		
		try {
			PreparedStatement stmt = this.manager.getConnection().prepareStatement(SQL_INSERT_MEDICAL_RECORD);
			stmt.setInt(1, id);
			stmt.setInt(2, patientId);
			stmt.setInt(3, doctorId);
			stmt.setString(4, cManager.cipher(weight));
			stmt.setString(5, cManager.cipher(height));
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addPrescription(int idMedRec, String medName, String presDate) {
				
		try {
			PreparedStatement stmt = this.manager.getConnection().prepareStatement(SQL_INSERT_MEDICATION);
			stmt.setInt(1, idMedRec);
			stmt.setString(2, cManager.cipher(medName));
			stmt.setString(3, cManager.cipher(presDate));
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addDiagnosis(int medRecId, String diagnosis, String diaDate, String diaDesc) {
		
		try {
			PreparedStatement stmt = this.manager.getConnection().prepareStatement(SQL_INSERT_DIAGNOSIS);
			stmt.setInt(1, medRecId);
			stmt.setString(2, cManager.cipher(diagnosis));
			stmt.setString(3, cManager.cipher(diaDate));
			stmt.setString(4, cManager.cipher(diaDesc));
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addTreatment(int medRecId, String treatment, String treDate, String treDesc) {
		
		try {
			PreparedStatement stmt = this.manager.getConnection().prepareStatement(SQL_INSERT_TREATMENT);
			stmt.setInt(1, medRecId);
			stmt.setString(2, cManager.cipher(treatment));
			stmt.setString(3, cManager.cipher(treDate));
			stmt.setString(4, cManager.cipher(treDesc));
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void changeHeight(Integer patientId, String height) {
		try {
			PreparedStatement stmt = this.manager.getConnection().prepareStatement(SQL_CHANGE_HEIGHT);
			stmt.setString(1, cManager.cipher(height));
			stmt.setInt(2, patientId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void changeWeight(Integer patientId, String weight) {
		try {
			PreparedStatement stmt = this.manager.getConnection().prepareStatement(SQL_CHANGE_WEIGHT);
			stmt.setString(1, cManager.cipher(weight));
			stmt.setInt(2, patientId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
		
	public List<PatientView> getDoctorPatients(String doctorId) {
		List<PatientView> result = new ArrayList<>();
		ResultSet resultSet = null; 
		try {
			PreparedStatement stmt = this.manager.getConnection().prepareStatement(SQL_GET_DOCTOR_PATIENTS);
			stmt.setString(1, doctorId);
			resultSet = stmt.executeQuery();
			
			
			
			while(resultSet.next()) {
				Integer userId = Integer.parseInt(resultSet.getString("PatientId"));
				String name = cManager.decipher(resultSet.getString("Name"));
				result.add(new PatientView(userId, name));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// FIXME return null or empty
		return result;
	}
	
	public String getHashedPassword(Integer userId) {
		
		String hashedPass = null;
		ResultSet resultSet = null; 
		
		try {
			// Get hashed password
			PreparedStatement stmt = this.manager.getConnection().prepareStatement(SQL_SELECT_AUTH);
			stmt.setInt(1, userId);
			resultSet = stmt.executeQuery();
			resultSet.next();
			hashedPass = resultSet.getString("HashedPass");
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		}
		return hashedPass;
	}
	
	public MedicalRecord getMedicalRecord(String patientId) {
		try {
			
			int searchParam = Integer.parseInt(patientId);
			
			//construct a medical record
			//first get the medical record
			PreparedStatement stmt = this.manager.getConnection().prepareStatement(SQL_GET_MEDICAL_RECORD);
			stmt.setInt(1, searchParam);
			ResultSet result = stmt.executeQuery();
			
			if (result.next()) {
			
				Integer recordId = result.getInt("Id");
				String patientName = getPatientName(searchParam);
				Integer doctorId = result.getInt("DoctorId");
				Integer height = Integer.parseInt(cManager.decipher(result.getString("Height")));
				Integer weight = Integer.parseInt(cManager.decipher(result.getString("Weight")));
				
				//then get the history from the medical record
				List<Prescription> prescription = getPrescription(recordId);
				List<Diagnosis> diagnosis = getDiagnosis(recordId);		
				List<Treatment> treatment = getTreatment(recordId);
				
				// curtia ir buscar nome do doutor provavelmente mas ja se faz isso
				return new MedicalRecord(recordId, searchParam, doctorId, patientName, height, weight, prescription, diagnosis, treatment);
			}
			
			
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// FIXME return null or empty
		return null;
	}
	
	public Integer getAllMedicalRecordsNum() {
		Integer size = 0;
		try {
			PreparedStatement stmt = this.manager.getConnection().prepareStatement(SQL_GET_ALL_MEDICAL_RECORD);
			ResultSet result = stmt.executeQuery();
						
			if (result != null) 
			{
			  result.last();    // moves cursor to the last row
			  size = result.getRow(); // get row id 
			}
						
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// FIXME return null or empty
		return size;
	}

	public String getPatientName(Integer userId) {
		
		PreparedStatement stmt;
		String name = null;
		try {
			stmt = this.manager.getConnection().prepareStatement(SQL_GET_USER_NAME);
			stmt.setInt(1, userId);
			ResultSet result = stmt.executeQuery();
			
			if(result.next()) {
				name = cManager.decipher(result.getString("Name"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return name;
	}

	@SuppressWarnings("deprecation")
	public List<Prescription> getPrescription(int medRecId){
		
		List<Prescription> list = new ArrayList<Prescription>();
		
		PreparedStatement stmt;
		try {
			stmt = this.manager.getConnection().prepareStatement(SQL_GET_MEDICATION);
			stmt.setInt(1, medRecId);
			ResultSet result = stmt.executeQuery();
			
			while(result.next()) {
				String name = cManager.decipher(result.getString("Name"));
				String date = cManager.decipher(result.getString("PrescriptionDate"));
				int year = Integer.parseInt(date.substring(0, 4));
				int month = Integer.parseInt(date.substring(4, 6));
				int day = Integer.parseInt(date.substring(6, 8));
				list.add(new Prescription(name, new Date(year - 1900, month - 1, day)));
			}


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return list;
		
	}
	
	@SuppressWarnings("deprecation")
	public List<Diagnosis> getDiagnosis(int medRecId){
		List<Diagnosis> list = new ArrayList<Diagnosis>();
		
		PreparedStatement stmt;
		try {
			stmt = this.manager.getConnection().prepareStatement(SQL_GET_DIAGNOSIS);
			stmt.setInt(1, medRecId);
			ResultSet result = stmt.executeQuery();
			
			while(result.next()) {
				String name = cManager.decipher(result.getString("Name"));
				String description = cManager.decipher(result.getString("Description"));
				String date = cManager.decipher(result.getString("DiagnosisDate"));
				int year = Integer.parseInt(date.substring(0, 4));
				int month = Integer.parseInt(date.substring(4, 6));
				int day = Integer.parseInt(date.substring(6, 8));
				list.add(new Diagnosis(name, new Date(year - 1900, month - 1, day), description));
			}


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return list;
	}
	
	@SuppressWarnings("deprecation")
	public List<Treatment> getTreatment(int medRecId){
		List<Treatment> list = new ArrayList<Treatment>();
		
		PreparedStatement stmt;
		try {
			stmt = this.manager.getConnection().prepareStatement(SQL_GET_TREATMENT);
			stmt.setInt(1, medRecId);
			ResultSet result = stmt.executeQuery();
			
			while(result.next()) {
				String name = cManager.decipher(result.getString("Name"));
				String description = cManager.decipher(result.getString("Description"));
				String date = cManager.decipher(result.getString("TreatmentDate"));
				int year = Integer.parseInt(date.substring(0, 4));
				int month = Integer.parseInt(date.substring(4, 6));
				int day = Integer.parseInt(date.substring(6, 8));
				list.add(new Treatment(name, new Date(year - 1900, month - 1, day), description));
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return list;
	}
	
	public List<User> getUsers(){
		
		List<User> result = new ArrayList<>();
		ResultSet resultSet = null; 
		
		try {
			PreparedStatement stmt = this.manager.getConnection().prepareStatement(SQL_SELECT_USER);
			resultSet = stmt.executeQuery();

			while(resultSet.next()) {
				Integer userId = Integer.parseInt(resultSet.getString("Id"));
				String name = cManager.decipher(resultSet.getString("Name"));
				String type = cManager.decipher(resultSet.getString("Type"));
				String birthDateStr = cManager.decipher(resultSet.getString("BirthDate"));
				Date birthDate = new Date(Integer.parseInt(birthDateStr));
				result.add(new User(userId, name, type, birthDate));
			}
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	
	public void close() throws SQLException {
		this.manager.closeConnection();
	}
	
	public void runTestScript(String filename) {
		
		System.out.println("Create test data for the database");
		
		try {
			BufferedReader br = new BufferedReader(new FileReader("resources/" + filename));
			String command; 
			while ((command = br.readLine()) != null) {
				String[] split = command.split(":");
				switch(split[0]) {
				case "createUser":
					Integer userId = Integer.parseInt(split[1]);
					String name = split[2];
					String type = split[3];
					String birthDate = split[4];
					String hashedPass = saltAndHashPass(split[5], userId);
					registerUser(userId, name, type, birthDate, hashedPass);
					break;
				case "createMD":
					Integer recordId = Integer.parseInt(split[1]);
					Integer patientId = Integer.parseInt(split[2]);
					Integer doctorId = Integer.parseInt(split[3]);
					String weight = split[4];
					String height = split[5];
					addMedicalRecord(recordId, patientId, doctorId, weight, height);
					break;
				case "createPrescription":
					Integer MDId1 = Integer.parseInt(split[1]);
					String medName = split[2];
					String presDate = split[3];
					addPrescription(MDId1, medName, presDate);
					break;
				case "createDiagnosis":
					Integer MDId2 = Integer.parseInt(split[1]);
					String diaName = split[2];
					String diaDesc = split[3];
					String diaDate = split[4];
					addDiagnosis(MDId2, diaName, diaDate, diaDesc);
					break;
				case "createTreatment":
					Integer MDId3 = Integer.parseInt(split[1]);
					String treName = split[2];
					String treDesc = split[3];
					String treDate = split[4];
					addTreatment(MDId3, treName, treDate, treDesc);
					break;
				}
				System.out.println(command); 
			}
			br.close();
		} catch (IOException e) {
			System.out.println("ERROR: Test file not found!");
		} 
	}

	private String saltAndHashPass(String password, Integer userId) {
		String hashedPass = null;
		try {
			MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
			String saltedPass = password + userId.toString();
			byte[] hashedPassBytes = sha256.digest(saltedPass.getBytes());
			hashedPass = new String(hashedPassBytes);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hashedPass;
	}
	
}
