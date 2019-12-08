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
		System.out.println("Hi! Welcome to SIRS Medical Record System.");
		// Variable used to store user input
		String userInput;
		// First loop to check if user is registered or not and then executes login or register, respectively
		Boolean exitSwitch = false;
		while(!exitSwitch) {
			System.out.print("Are you a registered user?: (Y = Yes, N = No)\n>> ");
			userInput = scanner.nextLine().split(" ")[0]; // FIXME NOT SANITIZING USER INPUT
			switch(userInput) {
				case "Y":
				case "y":
				case "yes":
				case "Yes":
					try {
						loginUser();
						user.runApp(inStream, outStream, scanner);
					} catch (NoSuchAlgorithmException | ClassNotFoundException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					exitSwitch = true;
					break;
				case "N":
				case "n":
				case "No":
				case "no":
					try {
						if(registerUser()) {
							user.runApp(inStream, outStream, scanner);
						}
						else {
							quitClient();
						}
					} catch (ClassNotFoundException | NoSuchAlgorithmException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					exitSwitch = true;
					break;
				default:
					System.out.println("Invalid instruction!");
			}
		}
		quitClient();
	}

	private static void loginUser() throws NoSuchAlgorithmException, IOException, ClassNotFoundException {
		// Ask user e-mail
		System.out.print("What's your username (e-mail)?\n>> ");
		String email = scanner.nextLine(); // FIXME NOT SANITIZING USER INPUT
		// Ask user password
		System.out.print("Enter your password:\n>> ");
		String password = scanner.nextLine(); // FIXME NOT SANITIZING USER INPUT
		// Check login result
		UserView loginResult = loginRequest(email, password);
		if(!loginResult.equals(null)) {
			user = loginResult;
			System.out.println("Hello " + user.getName() + "!");
		} else {
			System.out.println("Wrong credentials.");
			quitClient();
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

	private static Boolean registerUser() throws IOException, ClassNotFoundException, NoSuchAlgorithmException {
		// Pedir o tipo de utilizador
		String type = null;
		Boolean exitSwitch = false;
		while(!exitSwitch) {
			System.out.print("Are you a doctor, patient or staff?\n"
							+ "1) Patient\n"
							+ "2) Staff\n"
							+ "3) Doctor\n"
							+ ">> ");
			String typeOption = scanner.nextLine().split(" ")[0]; //FIXME NOT SANITIZING USER INPUT
			switch(typeOption) {
			case "1":
				type = "Patient";
				exitSwitch = true;
				break;
			case "2":
				type = "Staff";
				exitSwitch = true;
				break;
			case "3":
				type = "Doctor";
				exitSwitch = true;
				break;
			default:
				System.out.println("Invalid instruction!");
			}
		}
		// Ask user name
		System.out.print("What's your name?\n>> ");
		String username = scanner.nextLine(); //FIXME NOT SANITIZING USER INPUT
		// Ask user citizen Id
		System.out.print("What's your citizen Id?\n>> ");
		String userId = scanner.nextLine(); //FIXME NOT SANITIZING USER INPUT
		// Ask user e-mail
		System.out.print("Enter your e-mail:\n>> ");
		String email = scanner.nextLine(); //FIXME NOT SANITIZING USER INPUT
		if(!email.matches("^([a-z0-9_-]+\\.)*[a-z0-9_-]+@[a-z0-9_-]+(\\.[a-z0-9_-]+)*\\.[a-z]{2,6}$")) { //FIXME regex sacado
			System.out.println("Wrong e-mail format.");
			return false;
		}
		// Ask password
		System.out.print("Enter a password:\n>> ");
		String password = scanner.nextLine(); //FIXME NOT SANITIZING USER INPUT
		// Confirm password
		System.out.print("Confirm your password by reentering it:\n>> ");
		String confirmPassword = scanner.nextLine(); //FIXME NOT SANITIZING USER INPUT
		// If passwords match, ask server to make the user registration
		if(password.equals(confirmPassword)) {
			// Send registration request
			outStream.writeObject("-registerUser");
			// Send user e-mail
			outStream.writeObject(userId);
			if(inStream.readObject().equals("New user")) {
				// Send user e-mail
				outStream.writeObject(email);
				// Send username
				outStream.writeObject(username);
				// Send user type
				outStream.writeObject(type);
				// Send plain text password
				outStream.writeObject(password);
				// Checks if server accepts our password (strength-wise)
				String passStrength = (String) inStream.readObject();
				if(passStrength.equals("Strong password")) {
			        // Login
					UserView loginResult = loginRequest(email, password);
					if(!loginResult.equals(null)) {
						user = loginResult;
						System.out.println("You are now registered in SIRS Medical Records Systems.");
						System.out.println("Hello " + user.getName() + "!");
						return true;
					} else {
						System.out.println("Something went wrong with your registration");
						return false;
					}
				}
				else {
					System.out.println("Your password doesn't meet at least one of the following requirements:\n"
										+ "  - Must have a minimum of 12 characters\n"
										+ "  - Must contain at least 1 upper case letter\n"
										+ "  - Must contain at least 1 lower case letter\n"
										+ "  - Must contain at least 1 number\n"
										+ "  - Can't contain any name of yours\n"
										+ "  - Can't contain words that appear in your email");
					return false;
				}
			}
			else {
				System.out.println("User with such e-mail already exists");
				return false;
			}
		}
		else {
			System.out.println("Passwords don't match.");
			return false;
		}
		
	}
	
	private static void quitClient() {
		try {
			outStream.writeObject("-quit");
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
