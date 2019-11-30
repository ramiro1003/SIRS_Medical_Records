package ulisboa.tecnico.sirs.server.database;

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
	private static final String SQL_INSERT_AUTH = "INSERT INTO Auth (Email, HashedPass) VALUES (?, ?)";


	public DBGateway() {
		//Connection to database
		manager = new DBManager();
		manager.setProperties("jdbc:mariadb://localhost:3306/sirs", "seed", "admin");
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

	public List<User> getUsers(){

		List<User> result = new ArrayList<>();

		ResultSet resultSet = null; 

		try(PreparedStatement preparedStatement = 
				this.manager.getConnection().prepareStatement(SQL_SELECT_USERS)){

			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
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
