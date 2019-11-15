package ulisboa.tecnico.sirs.server.database;

import java.io.IOException;
import java.sql.SQLException;

public class DBGateway {

	private DBManager manager;

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
		
	}

}
