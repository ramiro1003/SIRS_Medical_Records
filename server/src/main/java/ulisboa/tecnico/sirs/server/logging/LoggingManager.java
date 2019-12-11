package ulisboa.tecnico.sirs.server.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggingManager {
	
	public static void writeLog(String log, String username) throws IOException {
		
		System.out.println("EMTREIIIIIIIIIIIIIIII");
		
		LocalDate current = LocalDate.now();
		
		//check if a new file is needed
		File f = new File("resources/logs/" + current.toString());
		if(!f.exists()) {
			f = createLogFile("resources/logs/" + current.toString());
		}
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
		out.write(dtf.format(now) + " | " + username + ": " +  log + "\n");
		out.close();
	}
	
	private static File createLogFile(String filename) {
		File f = new File(filename);
		return f;
	}
}
