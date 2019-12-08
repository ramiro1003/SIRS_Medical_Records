package ulisboa.tecnico.sirs.server.database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ulisboa.tecnico.sirs.crypto.CriptographyManager;
import  ulisboa.tecnico.sirs.domain.*;

public class DBGateway {

	private static final String SQL_SELECT_USERS = "SELECT * FROM User";
	private static final String SQL_INSERT_USER = "INSERT INTO User (UserId, Email, Name, Type, BirthDate) VALUES (?, ?, ?, ?, ?)";
	private static final String SQL_SELECT_AUTH = "SELECT HashedPass FROM Auth WHERE Email=?";
	private static final String SQL_INSERT_AUTH = "INSERT INTO Auth (Email, HashedPass) VALUES (?, ?)";
	private static final String SQL_UPDATE_AUTH = "UPDATE Auth SET HashedPass=? WHERE Email=?";
	private static final String SQL_GET_MEDICAL_RECORD = "SELECT * FROM MedicalRecord WHERE RecordId=?";
	private static final String SQL_INSERT_MEDICAL_RECORD = "INSERT INTO MedicalRecord (Id, PatientId, Weight, Height) VALUES (?,?,?,?)";
	private static final String SQL_INSERT_MEDICATION = "INSERT INTO Medication (MedicalRecordId, Name, PrescriptionDate) VALUES (?,?,?)";

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
			manager.createConnection();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e1) {
			System.out.println("Error connection to database");
		}

		try {
			manager.createDefaultDB();
		} catch (IOException e2) {
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

	public List<User> getUsers(){
		
		List<User> result = new ArrayList<>();
		ResultSet resultSet = null; 
		
		try {
			PreparedStatement stmt = this.manager.getConnection().prepareStatement(SQL_SELECT_USERS);
			resultSet = stmt.executeQuery();

			while(resultSet.next()) {
				int userId = Integer.parseInt(cManager.decipher(resultSet.getString("UserId")));
				String email = cManager.decipher(resultSet.getString("Email"));
				String name = cManager.decipher(resultSet.getString("Name"));
				String type = cManager.decipher(resultSet.getString("Type"));
				String birthDateStr = cManager.decipher(resultSet.getString("BirthDate"));
				Date birthDate = new Date(Integer.parseInt(birthDateStr));
				result.add(new User(userId, email, name, type, birthDate));
			}
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	
	public String getHashedPassword(String email) {
		
		String hashedPass = null;
		ResultSet resultSet = null; 
		
		try {
			// Get hashed password
			PreparedStatement stmt = this.manager.getConnection().prepareStatement(SQL_SELECT_AUTH);
			stmt.setString(1, email);
			resultSet = stmt.executeQuery();
			resultSet.next();
			hashedPass = resultSet.getString("HashedPass");
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		}
		return hashedPass;
	}
	
	public void updateUserPassword(String email, String newPass) {
		try{
			// Update Auth table
			PreparedStatement stmt = this.manager.getConnection().prepareStatement(SQL_UPDATE_AUTH);
			stmt.setString(1, newPass);
			stmt.setString(2, email);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		}
	}
	
	public void registerUser(int userId, String email, String name, 
			String type, String birthDate, String hashedPass) {
				
		try{
			// Insert into User table
			PreparedStatement stmt = this.manager.getConnection().prepareStatement(SQL_INSERT_USER);
			stmt.setInt(1, userId);
			stmt.setString(2, cManager.cipher(email));
			stmt.setString(3, cManager.cipher(name));
			stmt.setString(4, cManager.cipher(type));
			stmt.setString(5, cManager.cipher(birthDate));
			stmt.executeUpdate();
			// Insert into Auth table
			stmt = this.manager.getConnection().prepareStatement(SQL_INSERT_AUTH);
			stmt.setString(1, email);
			stmt.setString(2, hashedPass);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		} catch (Exception e) {
			System.out.println("Problems ciphering!");
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
	
	public MedicalRecord getMedicalRecord(String patientId) {
		try {
			// Get record from recordId
			PreparedStatement stmt = this.manager.getConnection().prepareStatement(SQL_GET_MEDICAL_RECORD);
			stmt.setString(1, patientId);
			ResultSet result = stmt.executeQuery();
			Integer recordId = Integer.parseInt(cManager.decipher(result.getString("Id")));
			Integer userId = Integer.parseInt(cManager.decipher(result.getString("PatientId")));
			Integer height = Integer.parseInt(cManager.decipher(result.getString("Height")));
			Integer weight = Integer.parseInt(cManager.decipher(result.getString("Weight")));
			return new MedicalRecord(recordId, userId, height, weight);
			
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
	
	public void close() throws SQLException {
		this.manager.closeConnection();
	}
	
	public void runTestScript(String filename) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String command; 
			while ((command = br.readLine()) != null) {
				String[] split = command.split(" ");
				switch(split[0]) {
				case "createUser":
					int idUser = Integer.parseInt(split[1]);
					String email = split[2];
					String name = split[3];
					String type = split[4];
					//pode dar merda
					String birthDate = split[5];
					
					String hashedPass = getHashedPassword(split[6]);
					registerUser(idUser, email, name, type, birthDate, hashedPass);
					break;
				case "createMD":
					int idMD = Integer.parseInt(split[1]);
					int patientId = Integer.parseInt(split[2]);
					String weight = split[3];
					String height = split[4];
					addMedicalRecord(idMD, patientId, weight, height);
					break;
				case "createMedication":
					int idMedRec = Integer.parseInt(split[1]);
					String medName = split[2];
					String presDate = split[3];
					addMedication(idMedRec, medName, presDate);
					break;
				}
				System.out.println(command); 
			}
			br.close();
		} catch (IOException e) {
			System.out.println("ERROR: Test file not found!");
		} 
	}


}
