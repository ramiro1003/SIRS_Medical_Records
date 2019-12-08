package ulisboa.tecnico.sirs.server.logging;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggingManager {
	
	private final static String FILE_PATH = "log_file.txt";
	private BufferedWriter out;
	
	public LoggingManager() throws IOException {
		out = new BufferedWriter(new FileWriter(FILE_PATH, true));
	}
	
	public void writeLog(String log, String username) throws IOException {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		out.write(dtf.format(now) + " | " + username + ": " +  log + "\n");
	}
}
