package ulisboa.tecnico.sirs.server.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class DBManager {

	private Connection connection = null;
	private String url;
	private String username;
	private String password;
	private static final String DB_SETUP_SCRIPT = "setupscript.txt";

	public void setProperties(String url, String username, String password) {
		this.url = url;
		this.username = username;
		this.password = password;
	}

	public void createConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		connection = DriverManager.getConnection(url, username, password);
	}

	public void closeConnection() throws SQLException {
		connection.close();
	}
	
	public Connection getConnection() {
		return this.connection;
	}

	@SuppressWarnings("resource")
	public void createDefaultDB() throws IOException {
		
		File f = new File(DB_SETUP_SCRIPT);
		FileInputStream inputstream = new FileInputStream(f);
		Scanner sc = new Scanner(inputstream).useDelimiter("\\A");
	    String queryStatement = sc.hasNext() ? sc.next() : "";
	    try (Statement statement = this.connection.createStatement()) {
	        statement.execute(queryStatement);
	    } catch (SQLException e) {
	    	e.printStackTrace();
		    inputstream.close();
	    }
	    inputstream.close();
	}

}
