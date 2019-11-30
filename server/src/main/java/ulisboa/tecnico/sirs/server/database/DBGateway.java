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
	private static final String SQL_INSERT_USER = "INSERT INTO User (Email, Name, Type) VALUES (?, ?, ?)";
	private static final String SQL_SELECT_AUTH = "SELECT HashedPass FROM Auth WHERE Email=?";
	private static final String SQL_INSERT_AUTH = "INSERT INTO Auth (Email, HashedPass) VALUES (?, ?)";

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
		String path = "dbconfig.txt";
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			String dbName = reader.readLine();
			String dbPwd = reader.readLine();
			reader.close();
			
			this.db = dbName.split("=")[1];
			this.password = dbPwd.split("=")[1];
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
				String email = resultSet.getString("Email");
				String name = resultSet.getString("Name");
				String type = resultSet.getString("Type");
				
				switch(type.toLowerCase()) {
					case "admin":
						result.add(new Admin(email, name));
						break;
					case "patient":
						result.add(new Patient(email, name));
						break;
					case "doctor":
						result.add(new Doctor(email, name));
						break;
					case "staff":
						result.add(new Staff(email, name));
						break;
				}
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
	
	public void registerUser(String email, String name, String type, String hashedPass) {
		try{
			// Insert into User table
			PreparedStatement stmt = this.manager.getConnection().prepareStatement(SQL_INSERT_USER);
			stmt.setString(1, email);
			stmt.setString(2, name);
			stmt.setString(3, type);
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

	public void close() throws SQLException {
		this.manager.closeConnection();
	}

}
