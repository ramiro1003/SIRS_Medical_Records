package ulisboa.tecnico.sirs.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class App 
{
	
	private static Scanner scanner;
	private static String username;
	private static String ip;
	private static int port;
	private static ObjectInputStream inStream;
	private static ObjectOutputStream outStream;
	
	@SuppressWarnings("resource")
	public static void main( String[] args )
	{
		if (args.length != 1) { //FIXME pode haver outras cenas a toa que sejam válidas para printar esta mensagem de erro
			System.out.println("System usage: -Dexec.args=\"{serverIp}:{serverPort}\"");
			System.exit(0);
		}
		String[] serverLocation = args[0].split(":");
		// Check if IP format is valid
		if (serverLocation[0].matches("^(?:(?:\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.){3}(?:\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])$")) { //FIXME NAO TESTEI O REGEX, É SACADO
			ip = serverLocation[0];
		} else {
			System.out.println("Invalid IPv4 format");
			System.exit(0);
		}
		port = Integer.parseInt(serverLocation[1]);
		try {
			
			scanner = new Scanner(System.in);
			Socket socket = new Socket(ip, port);

			outStream = new ObjectOutputStream(socket.getOutputStream());
			inStream = new ObjectInputStream(socket.getInputStream());


		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Unable to connect to host :(");
			System.exit(0);
		}
		run();
	}

	private static void run() {
		
		System.out.println("Hi! Welcome to SIRS Medical Record System.");
		
		String inputOption;
		Boolean exitSwitch = false;
		// First loop to check if user is registered or not and then executes login or register, respectively
		while (!exitSwitch) {
			System.out.print("Are you a registered user?: (Y = Yes, N = No)\n>> ");
			inputOption = scanner.nextLine().split(" ")[0]; //FIXME NOT SANITIZING USER INPUT
			switch(inputOption) {
				case "Y":
					loginUser();
					exitSwitch = true;
					break;
				case "y":
					loginUser();
					exitSwitch = true;
					break;
				case "N":
					try {
						registerUser();
					} catch (ClassNotFoundException | NoSuchAlgorithmException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					exitSwitch = true;
					break;
				case "n":
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
		while (!quit) {
			System.out.print("Please choose the number of what you want to perform and press enter:\n"
							+ "1) List Medical Records\n"
							+ "2) Read a Medical Record\n"
							+ "0) Quit\n"
							+ ">> ");
			
			inputOption = scanner.nextLine().split(" ")[0]; //FIXME NOT SANITIZING USER INPUT

			switch(inputOption) {
			case "1":
				listMDClient();
				break;
			case "2":
				readMDClient();
				break;
			case "0":
				System.out.print("Sure you want to quit? (Y = Yes, N = No)\n>> ");
				String conf = scanner.nextLine().split(" ")[0]; //FIXME NOT SANITIZING USER INPUT
				switch(conf) {
					case "Y":
						quit = true;
						quitClient();
						break;
					case "y":
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
	}
	
	private static void loginUser() {	
		System.out.print("What's your username (e-mail)?\n>> ");
		username = scanner.nextLine().split(" ")[0]; // FIXME NOT SANITIZING USER INPUT
		
		try {
			outStream.writeObject(username);
			Boolean loginResult = (Boolean) inStream.readObject();
			if (!loginResult) {
				System.out.println("User not found!");
				System.exit(0);
			} else {
				System.out.println("Hello " + username + "!");
			}
		} catch (IOException | ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
	}

	private static void registerUser() throws IOException, ClassNotFoundException, NoSuchAlgorithmException {
		// FIXME bue simples fecha sempre quando ha falhas tipo email ja existe ou password nao é igual à confirmação
		// Pedir o tipo de utilizador
		String type = null;
		Boolean exitSwitch = false;
		while (!exitSwitch) {
			System.out.print("Are you a doctor, patient or staff?\n"
							+ "1) Patient\n"
							+ "2) Staff\n"
							+ "3) Doctor\n"
							+">> ");
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
		// Ask password
		System.out.print("Enter a password:\n>> ");
		String password = scanner.nextLine().split(" ")[0]; //FIXME NOT SANITIZING USER INPUT
		// Confirm password
		System.out.print("Confirm your password by reentering it:\n>> ");
		String confirmPassword = scanner.nextLine().split(" ")[0]; //FIXME NOT SANITIZING USER INPUT
		// If passwords match, ask server to make the user registration
		if (password.equals(confirmPassword)) {
			// Send registration request
			outStream.writeObject("RegisterUser");
			// Send user e-mail
			outStream.writeObject(email);
			if (inStream.readObject().equals("New email")) {
				// Send username
				outStream.writeObject(username);
				// Send user type
				outStream.writeObject(type);
				MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
				String saltedPass = password + email;
				byte[] hashedPassBytes = sha256.digest(saltedPass.getBytes());
				String hashedPass = new String(hashedPassBytes);
		        // Send hashed password
		        outStream.writeObject(hashedPass);
		        System.out.println("You are now registered in SIRS Medical Records Systems");
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
			outStream.writeObject("-l");
			System.out.println(inStream.readObject());
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void readMDClient() {
		try {
			outStream.writeObject("-r");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void quitClient() {
		try {
			outStream.writeObject("quit");
			outStream.writeObject(username);
			System.out.println("Bye " + username + "!");
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
}
