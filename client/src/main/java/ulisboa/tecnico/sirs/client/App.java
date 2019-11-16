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
		scanner = new Scanner(System.in);
		username = args[1];
		String[] serverLocation = args[0].split(":");
		ip = serverLocation[0];
		port = Integer.parseInt(serverLocation[1]);
		try {
			
			Socket socket = new Socket(ip, port);

			outStream = new ObjectOutputStream(socket.getOutputStream());
			inStream = new ObjectInputStream(socket.getInputStream());

			outStream.writeObject(username);

			Boolean loginResult = (Boolean) inStream.readObject();
			if (!loginResult) {
				System.out.println("User not found!");
				System.exit(0);
			} else {
				System.out.println("Welcome " + username + "!");
			}


		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		run();
	}

	private static void run() {

		String inputOption;
		Boolean quit = false;
		while (!quit) {
			System.out.print("\nPlease choose the number of what you want to perform and press enter:\n"
							+ "1) List Medical Records\n"
							+ "2) Read a Medical Record\n"
							+ "0) Quit\n"
							+ ">> ");
			
			inputOption = scanner.nextLine().split(" ")[0];

			switch(inputOption) {
			case "1":
				listMDClient();
				break;
			case "2":
				readMDClient();
				break;
			case "0":
				System.out.print("Sure you want to quit? (Y = Yes, N = No)\n>> ");
				String conf = scanner.nextLine();
				if (conf.equals("Y") || conf.contentEquals("y")) {
					quit = true;
					quitClient(username);
					System.exit(0);
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
