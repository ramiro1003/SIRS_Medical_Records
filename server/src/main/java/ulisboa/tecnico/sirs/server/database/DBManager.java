package ulisboa.tecnico.sirs.server.database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.ibatis.jdbc.ScriptRunner;

public class DBManager {

	private Connection connection = null;
	private String url;
	private String username;
	private String password;

	private static final String DB_SETUP_SCRIPT = "resources/setupscript.sql";

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
		//Initialize the script runner
		ScriptRunner sr = new ScriptRunner(this.connection);
		//Creating a reader object
		Reader reader = new BufferedReader(new FileReader(DB_SETUP_SCRIPT));
		//Running the script
		sr.runScript(reader);
	}

}
