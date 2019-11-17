package ulisboa.tecnico.sirs.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

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
			System.out.println("System usage: {serverIp}:{serverPort}");
			System.exit(0);
		}
		String[] serverLocation = args[0].split(":");
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
			System.out.print("Are you a registred user?: (Y = Yes, N = No)\n>> ");
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
					System.out.println("Cannot register yet! Try next time!");
					System.exit(0);
					registerUser(); //FIXME Must implement method
					exitSwitch = true;
					break;
				case "n":
					System.out.println("Cannot register yet! Try next time!");
					System.exit(0);
					registerUser();
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
						quitClient(username);
						System.exit(0);
						break;
					case "y":
						quit = true;
						quitClient(username);
						System.exit(0);
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
		System.out.print("What's your username (email)?\n>> ");
		username = scanner.nextLine().split(" ")[0]; //FIXME NOT SANITIZING USER INPUT
		
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

	private static void registerUser() {
		// TODO Auto-generated method stub
		
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
	
	private static void quitClient(String username2) {
		try {
			outStream.writeObject("quit");
			outStream.writeObject(username);
			System.out.println("Bye " + username + "!");
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
}
