package ulisboa.tecnico.sirs.server.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import org.apache.ibatis.jdbc.ScriptRunner;


public class DBManager {

	private Connection connection = null;
	private String url;
	private String username;
	private String password;
	private static final String DB_SETUP_SCRIPT = "setupscript.sql";

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

		/**
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
		 */
		//Initialize the script runner
		ScriptRunner sr = new ScriptRunner(this.connection);
		//Creating a reader object
		Reader reader = new BufferedReader(new FileReader(DB_SETUP_SCRIPT));
		//Running the script
		sr.runScript(reader);
	}

	public ResultSet getUsers() {
		
	}
}
