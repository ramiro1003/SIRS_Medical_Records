package ulisboa.tecnico.sirs.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import ulisboa.tecnico.sirs.library.domain.UserView;

import java.security.NoSuchAlgorithmException;


public class App 
{
	
	private static Scanner scanner;
	private static UserView user;
	private static String ip;
	private static int port;
	private static String context;
	private static ObjectInputStream inStream;
	private static ObjectOutputStream outStream;
	private static final String KEYSTORE_PATH = "resources/client.keyStore";
	
	
	public static void main( String[] args ) {
		if(args.length != 1) {
			System.out.println("System usage: mvn exec:java -Dexec.args=\"[serverIp]:[serverPort]:{context}\"\n"
								+ "You can either not specify context or specify using one of the two modes: \"operationroom\" or \"emergency\"");
			System.exit(0);
		}
		// Get server IP and port, and get context
		String[] input = args[0].split(":");
		// Check if application is being properly run with mvn exec
		if(input.length > 3 || input.length < 2) {
			System.out.println("System usage: mvn exec:java -Dexec.args=\"[serverIp]:[serverPort]:{context}\"\n"
								+ "You can either not specify context or specify using one of the two modes: \"operationroom\" or \"emergency\"");
			System.exit(0);
		}
		// Check if IP format is valid
		if(input[0].matches("^(?:(?:\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.){3}(?:\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])$")) { //FIXME Regex sacado
			ip = input[0];
		}
		else {
			System.out.println("Invalid IPv4 format");
			System.exit(0);
		}
		// Check if port is valid
		try {
			port = Integer.parseInt(input[1]);
		} catch (NumberFormatException e) {
			System.out.println("Port must be an integer");
			System.exit(0);
		}
		// Check if context is valid
		if(input.length == 3) {
			if(input[2].equals("operationroom") || input[2].equals("emergency")) {
				context = input[2];
			}
			else {
				System.out.println("You can either not specify context or specify using one of the two modes: \"operationroom\" or \"emergency\"");
				System.exit(0);
			}
		}
		else {
			context = "default";
		}
		// Set system keystore
		System.setProperty("javax.net.ssl.trustStore", KEYSTORE_PATH);
		System.setProperty("javax.net.ssl.trustStorePassword", "sirssirs");
		
		try {
			// Instantiate scanner to get user input
			scanner = new Scanner(System.in);
			// Create a socket for TLS connection with the server
			SocketFactory sf = SSLSocketFactory.getDefault();
			SSLSocket socket = (SSLSocket) sf.createSocket(ip, port);
			// Connection streams
			outStream = new ObjectOutputStream(socket.getOutputStream());
			inStream = new ObjectInputStream(socket.getInputStream());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Unable to connect to host :(");
			System.exit(0);
		}
		// Run Application
		run();
	}

	private static void run() {
		// Welcome message
		System.out.println("Hi! Welcome to SIRS Medical Record System.\n"
							+ "Please login with your credentials:");
		// Ask user e-mail
		System.out.print("What's your Id (Citizen Card)?\n>> ");
		String userId = scanner.nextLine(); // FIXME NOT SANITIZING USER INPUT
		// Ask user password
		System.out.print("Enter your password:\n>> ");
		String password = scanner.nextLine(); // FIXME NOT SANITIZING USER INPUT
		try {
			// Check login result
			if(loginRequest(Integer.parseInt(userId), password)) {
				System.out.println("Hello " + user.getName() + "!");
				user.runApp(inStream, outStream, scanner);
			} else {
				System.out.println("Wrong credentials.");
				quitClient();
			}
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static Boolean loginRequest(Integer userId, String password) throws ClassNotFoundException, IOException {
		// Send login request
		outStream.writeObject("-loginUser");
		// Send userId
		outStream.writeObject(userId);
		// Send hash of password + salt(email)
		outStream.writeObject(password);
		// Check login result
		String loginResult = (String) inStream.readObject();
		if(loginResult.equals("User logged in")) {
			UserView userView = (UserView) inStream.readObject();
			user = userView;
			return true;
		}
		return false;
	}
	
	private static void quitClient() {
		try {
			outStream.close();
			inStream.close();
			System.exit(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);;
		}
	}
	
}
