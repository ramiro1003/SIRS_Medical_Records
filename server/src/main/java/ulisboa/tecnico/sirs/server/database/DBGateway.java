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


	public DBGateway() {
		//Connection to database
		manager = new DBManager();
		manager.setProperties("jdbc:mariadb://localhost:3306/sirs", "root", "admin");
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
				int id = resultSet.getInt("ID");
				String name = resultSet.getString("NAME");
				String type = resultSet.getString("TYPE");
				
				switch(type.toLowerCase()) {
					case "admin":
						result.add(new Admin(id, name));
						break;
					case "patient":
						result.add(new Patient(id, name));
						break;
					case "doctor":
						result.add(new Doctor(id, name));
						break;
					case "staff":
						result.add(new Staff(id, name));
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

	public void close() throws SQLException {
		this.manager.closeConnection();
	}

}
