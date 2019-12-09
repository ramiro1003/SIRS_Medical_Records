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
	private static ObjectInputStream inStream;
	private static ObjectOutputStream outStream;
	private static final String KEYSTORE_PATH = "resources/client.keyStore";
	
	@SuppressWarnings("resource")
	public static void main( String[] args ) {
		// Check if application is being properly run with mvn exec
		if(args.length != 1) {
			System.out.println("System usage: mvn exec:java -Dexec.args=\"{serverIp}:{serverPort}\"");
			System.exit(0);
		}
		// Get "server location": server IP and port
		String[] serverLocation = args[0].split(":");
		// Check if IP format is valid
		if(serverLocation[0].matches("^(?:(?:\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.){3}(?:\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])$")) { //FIXME Regex sacado
			ip = serverLocation[0];
		}
		else {
			System.out.println("Invalid IPv4 format");
			System.exit(0);
		}
		// Check if port is valid
		try {
			port = Integer.parseInt(serverLocation[1]);
		} catch (NumberFormatException e) {
			System.out.println("Port must be an integer");
			System.exit(0);
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
							+ "Please login with your credentials:\n");
		// Ask user e-mail
		System.out.print("What's your username (e-mail)?\n>> ");
		String email = scanner.nextLine(); // FIXME NOT SANITIZING USER INPUT
		// Ask user password
		System.out.print("Enter your password:\n>> ");
		String password = scanner.nextLine(); // FIXME NOT SANITIZING USER INPUT
		try {
			// Check login result
			UserView loginResult = loginRequest(email, password);
			if(!loginResult.equals(null)) {
				user = loginResult;
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
	
	private static UserView loginRequest(String email, String password) throws ClassNotFoundException, IOException {
		// Send login request
		outStream.writeObject("-loginUser");
		// Send email
		outStream.writeObject(email);
		// Send hash of password + salt(email)
		outStream.writeObject(password);
		// Check login result
		UserView loginResult = (UserView) inStream.readObject();
		return loginResult;
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
