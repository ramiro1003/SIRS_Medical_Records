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
	private static final String SQL_GET_MEDICAL_RECORD = "SELECT * FROM MedicalRecord INNER JOIN User ON MedicalRecord.PatiendId = User.Id WHERE MedicalRecord.PatientId = ?";
	private static final String SQL_INSERT_MEDICAL_RECORD = "INSERT INTO MedicalRecord (Id, PatientId, Weight, Height) VALUES (?,?,?,?)";
	private static final String SQL_CHANGE_WEIGHT = "UPDATE MedicalRecord SET Weight=? VALUES (?) WHERE MedicalRecord.PatientId = ?";
	private static final String SQL_CHANGE_HEIGHT = "UPDATE MedicalRecord SET Height=? VALUES (?) WHERE MedicalRecord.PatientId = ?";
	private static final String SQL_INSERT_MEDICATION = "INSERT INTO Medication (MedicalRecordId, Name, PrescriptionDate) VALUES (?,?,?)";
	private static final String SQL_GET_DOCTOR_PATIENTS = "SELECT * FROM MedicalRecord INNER JOIN User AS d ON MedicalRecord.DoctorId = b.Id"
														+ "INNER JOIN User AS p ON MedicalRecord.PatientId = a.Id"
														+ "WHERE MedicalRecord.DoctorId = ?";

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
	
	private void addMedicalRecord(int id, int patientId, String weight, String height) {
		
		//cipher strings
		
		try {
			PreparedStatement stmt = this.manager.getConnection().prepareStatement(SQL_INSERT_MEDICAL_RECORD);
			stmt.setInt(1, id);
			stmt.setInt(2, patientId);
			stmt.setString(3, cManager.cipher(weight));
			stmt.setString(4, cManager.cipher(height));
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void addMedication(int idMedRec, String medName, String presDate) {
		
		//cipher strings
		
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
	
	public void changeHeight(Integer patientId, String height) {
		// FIXME CHECK THIS
		try {
			PreparedStatement stmt = this.manager.getConnection().prepareStatement(SQL_CHANGE_HEIGHT);
			stmt.setInt(1, patientId);
			stmt.setString(2, cManager.cipher(height));
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void changeWeight(Integer patientId, String weight) {
		// FIXME CHECK THIS
		try {
			PreparedStatement stmt = this.manager.getConnection().prepareStatement(SQL_CHANGE_WEIGHT);
			stmt.setInt(1, patientId);
			stmt.setString(2, cManager.cipher(weight));
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
				Integer userId = Integer.parseInt(cManager.decipher(resultSet.getString("p.UserId")));
				String name = cManager.decipher(resultSet.getString("p.Name"));
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
			PreparedStatement stmt = this.manager.getConnection().prepareStatement(SQL_GET_MEDICAL_RECORD);
			stmt.setString(1, patientId);
			ResultSet result = stmt.executeQuery();
			
			// FIXME needs fixing according to new db schema 
			Integer recordId = Integer.parseInt(cManager.decipher(result.getString("Id")));
			Integer patientIdInt = Integer.parseInt(patientId);
			String patientName = result.getString(cManager.decipher(result.getString("Name")));
			Integer doctorId = Integer.parseInt(cManager.decipher(result.getString("DoctorId")));
			Integer height = Integer.parseInt(cManager.decipher(result.getString("Height")));
			Integer weight = Integer.parseInt(cManager.decipher(result.getString("Weight")));
			List<Prescription> prescription = null;
			List<Diagnosis> diagnosis = null;				//FIXME LISTS ARE NULL!
			List<Treatment> treatment = null;
			
			// curtia ir buscar nome do doutor provavelmente mas ja se faz isso
			
			return new MedicalRecord(recordId, patientIdInt, doctorId, patientName, height, weight, prescription, diagnosis, treatment);
			
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
	
	public void registerUser(Integer userId, String name, 
			String type, String birthDate, String hashedPass) {
				
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
	
	public void close() throws SQLException {
		this.manager.closeConnection();
	}
	
	public void runTestScript(String filename) {
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
					//pode dar merda
					String birthDate = split[4];
					String hashedPass = saltAndHashPass(split[5], userId);
					registerUser(userId, name, type, birthDate, hashedPass);
					break;
				case "createMD":
					Integer recordId = Integer.parseInt(split[1]);
					Integer patientId = Integer.parseInt(split[2]);
					String weight = split[3];
					String height = split[4];
					addMedicalRecord(recordId, patientId, weight, height);
					break;
				case "createMedication":
					Integer MDId = Integer.parseInt(split[1]);
					String medName = split[2];
					String presDate = split[3];
					addMedication(MDId, medName, presDate);
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
