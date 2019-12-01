package ulisboa.tecnico.sirs.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class App 
{
	
	private static Scanner scanner;
	private static String user;
	private static String ip;
	private static int port;
	private static ObjectInputStream inStream;
	private static ObjectOutputStream outStream;
	private static final String KEYSTORE_PATH = "client.keyStore";
	
	@SuppressWarnings("resource")
	public static void main( String[] args )
	{
		// Check if application is being properly run
		if(args.length != 1) {
			System.out.println("System usage: -Dexec.args=\"{serverIp}:{serverPort}\"");
			System.exit(0);
		}
		String[] serverLocation = args[0].split(":");
		// Check if IP format is valid
		if(serverLocation[0].matches("^(?:(?:\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.){3}(?:\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])$")) { //FIXME Regex sacado
			ip = serverLocation[0];
		} else {
			System.out.println("Invalid IPv4 format");
			System.exit(0);
		}
		port = Integer.parseInt(serverLocation[1]);
		
		System.setProperty("javax.net.ssl.trustStore", KEYSTORE_PATH);
		System.setProperty("javax.net.ssl.trustStorePassword", "sirssirs");
		
		try {
			
			scanner = new Scanner(System.in);

			SocketFactory sf = SSLSocketFactory.getDefault();
			SSLSocket socket = (SSLSocket) sf.createSocket(ip, port);
			
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
						registerUser();
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
		// User logged in, now running application
		Boolean quit = false;
		while(!quit) {
			System.out.print("Please choose the number of what you want to perform and press enter:\n"
							+ "1) List Medical Records\n"
							+ "2) Read a Medical Record\n"
							+ "0) Quit\n"
							+ ">> ");
			
			userInput = scanner.nextLine().split(" ")[0]; // FIXME NOT SANITIZING USER INPUT
			
			switch(userInput) {
			case "1":
				listMDClient();
				break;
			case "2":
				readMDClient();
				break;
			case "0":
				System.out.print("Sure you want to quit? (Y = Yes, N = No)\n>> ");
				String conf = scanner.nextLine().split(" ")[0]; // FIXME NOT SANITIZING USER INPUT
				switch(conf) {
					case "Y":
					case "y":
					case "yes":
					case "Yes":
						quit = true;
						quitClient();
						break;
					default:
				}				
				break;
			default:
				System.out.println("Invalid instruction!");
			}
		}
		try {
			outStream.close();
			inStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	private static void loginUser() throws NoSuchAlgorithmException, IOException, ClassNotFoundException {
		// Ask user e-mail
		System.out.print("What's your username (e-mail)?\n>> ");
		String email = scanner.nextLine().split(" ")[0]; // FIXME NOT SANITIZING USER INPUT
		// Ask user password
		System.out.print("Enter your password:\n>> ");
		String password = scanner.nextLine().split(" ")[0]; // FIXME NOT SANITIZING USER INPUT
		// Hash password + salt(email)
		String saltedPass = password + email;
		MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
		byte[] hashedPassBytes = sha256.digest(saltedPass.getBytes());
		String hashedPass = new String(hashedPassBytes);
		// Check login result
		if(loginRequest(email, hashedPass)) {
			user = email;
			System.out.println("Hello " + user + "!");
		} else {
			System.out.println("Wrong credentials.");
			System.exit(0);
		}
	}
	
	private static Boolean loginRequest(String email, String hashedPass) throws ClassNotFoundException, IOException {
		// Send login request
		outStream.writeObject("-loginUser");
		// Send email
		outStream.writeObject(email);
		// Send hash of password + salt(email)
		outStream.writeObject(hashedPass);
		// Check login result
		String loginResult = (String) inStream.readObject();
		if(loginResult.equals("Successful login")) {
			return true;
		} else {
			return false;
		}
	}

	private static void registerUser() throws IOException, ClassNotFoundException, NoSuchAlgorithmException {
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
		String username = scanner.nextLine().split(" ")[0]; //FIXME NOT SANITIZING USER INPUT
		// Ask user e-mail
		System.out.print("Enter your e-mail:\n>> ");
		String email = scanner.nextLine().split(" ")[0]; //FIXME NOT SANITIZING USER INPUT
		if(!email.matches("^([a-z0-9_-]+\\.)*[a-z0-9_-]+@[a-z0-9_-]+(\\.[a-z0-9_-]+)*\\.[a-z]{2,6}$")) {
			System.out.println("Wrong e-mail format.");
			System.exit(0);
		}
		// Ask password
		System.out.print("Enter a password:\n>> ");
		String password = scanner.nextLine().split(" ")[0]; //FIXME NOT SANITIZING USER INPUT
        if(password.length() < 12) {
        	System.out.print("Your password must follow these requirements:\n"
        						+ "\t-- Must have a minimum of 12 characters\n");
        	System.exit(0);
        }
		// Confirm password
		System.out.print("Confirm your password by reentering it:\n>> ");
		String confirmPassword = scanner.nextLine().split(" ")[0]; //FIXME NOT SANITIZING USER INPUT
		// If passwords match, ask server to make the user registration
		if(password.equals(confirmPassword)) {
			// Send registration request
			outStream.writeObject("-registerUser");
			// Send user e-mail
			outStream.writeObject(email);
			if(inStream.readObject().equals("New email")) {
				// Send username
				outStream.writeObject(username);
				// Send user type
				outStream.writeObject(type);
				// Hash password + salt(email) and send
				MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
				String saltedPass = password + email;
				byte[] hashedPassBytes = sha256.digest(saltedPass.getBytes());
				String hashedPass = new String(hashedPassBytes);
		        outStream.writeObject(hashedPass);
		        // Login
				if(loginRequest(email, hashedPass)) {
					user = email;
					System.out.println("You are now registered in SIRS Medical Records Systems.");
					System.out.println("Hello " + user + "!");
				} else {
					System.out.println("Something went wrong with your registration");
					System.exit(0);
				}
			}
			else {
				System.out.println("User with such e-mail already exists");
				System.exit(0);
			}
		}
		else {
			System.out.println("Passwords don't match.");
			System.exit(0);
		}
		
	}

	private static void listMDClient() {
		try {
			outStream.writeObject("-listMD");
			System.out.println(inStream.readObject());
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void readMDClient() {
		try {
			outStream.writeObject("-readMD");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void quitClient() {
		try {
			outStream.writeObject("-quit");
			outStream.writeObject(user);
			System.out.println("Bye " + user + "!");
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
}
