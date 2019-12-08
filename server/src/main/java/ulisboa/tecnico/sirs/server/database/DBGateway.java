package ulisboa.tecnico.sirs.server.database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import  ulisboa.tecnico.sirs.domain.*;

public class DBGateway {

	private DBManager manager;
	private static final String SQL_SELECT_USERS = "SELECT * FROM User";
	private static final String SQL_INSERT_USER = "INSERT INTO User (UserId, Email, Name, Type) VALUES (?, ?, ?, ?)";
	private static final String SQL_SELECT_AUTH = "SELECT HashedPass FROM Auth WHERE Email=?";
	private static final String SQL_INSERT_AUTH = "INSERT INTO Auth (Email, HashedPass) VALUES (?, ?)";
	private static final String SQL_UPDATE_AUTH = "UPDATE Auth SET HashedPass=? WHERE Email=?";
	private static final String SQL_GET_MEDICAL_RECORD = "SELECT * FROM MedicalRecord WHERE RecordId=?";

	
	private String db;
	private String password;
	

	public DBGateway() {
		
		getDatabaseCredentials();
		
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
				String userId = resultSet.getString("UserId");
				String email = resultSet.getString("Email");
				String name = resultSet.getString("Name");
				String type = resultSet.getString("Type");
				result.add(new User(userId, email, name, type));
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
	
	public void registerUser(String userId, String email, String name, String type, String hashedPass) {
		try{
			// Insert into User table
			PreparedStatement stmt = this.manager.getConnection().prepareStatement(SQL_INSERT_USER);
			stmt.setString(1, userId);
			stmt.setString(2, email);
			stmt.setString(3, name);
			stmt.setString(4, type);
			stmt.executeUpdate();
			// Insert into Auth table
			stmt = this.manager.getConnection().prepareStatement(SQL_INSERT_AUTH);
			stmt.setString(1, email);
			stmt.setString(2, hashedPass);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		}
	}
	
	public MedicalRecord getRecord(String requestedRecordId) {
		try {
			// Get record from recordId
			PreparedStatement stmt = this.manager.getConnection().prepareStatement(SQL_GET_MEDICAL_RECORD);
			stmt.setString(1, requestedRecordId);
			ResultSet result = stmt.executeQuery();
			Integer recordId = result.getInt("RecordId");
			Integer userId = result.getInt("UserId");
			String patientName = result.getString("PatientName");
			Integer age = result.getInt("Age");
			Integer height = result.getInt("Height");
			Integer weight = result.getInt("Weight");
			
			return new MedicalRecord(recordId, userId, patientName, age, height, weight);
			
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		}
		// FIXME return null or empty
		return null;
	}

	public void close() throws SQLException {
		this.manager.closeConnection();
	}

}
