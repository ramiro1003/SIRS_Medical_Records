package ulisboa.tecnico.sirs.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class App 
{
	
	private static Scanner sc;
	private static String username;
	private static String ip;
	private static int port;
	private static ObjectInputStream inStream;
	private static ObjectOutputStream outStream;
	
	@SuppressWarnings("resource")
	public static void main( String[] args )
	{
		sc = new Scanner(System.in);
		username = args[1];
		String[] st = args[0].split(":");
		ip = st[0];
		port = Integer.parseInt(st[1]);
		try {
			
			Socket socket = new Socket(ip, port);

			outStream = new ObjectOutputStream(socket.getOutputStream());
			inStream = new ObjectInputStream(socket.getInputStream());

			outStream.writeObject(username);

			Boolean loginResult = (Boolean) inStream.readObject();
			if (!loginResult) {
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

		String line;
		String[] split;
		Boolean quit = false;
		System.out.print(">>");
		while (!quit) {
			line = sc.nextLine();
			split = line.split(" ");

			switch (split[0]) {
			case "-l":
				listMDClient(split);
				break;
			case "-r":
				readMDClient(split);
				break;
			case "quit":
				System.out.print("Sure you want to quit? (Y = Yes, N = No)");
				String conf = sc.nextLine();
				if (conf.equals("Y")) {
					quit = true;
					quitClient(username);
					System.exit(0);
				}
				break;
			default:
				System.out.println("Invalid instruction!");
			}

			System.out.print(">>");
		}
		try {
			outStream.close();
			inStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void quitClient(String username2) {
		try {
			outStream.writeObject("quit");
			outStream.writeObject(username);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	private static void readMDClient(String[] split) {
		try {
			outStream.writeObject("-r");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void listMDClient(String[] split) {
		try {
			outStream.writeObject("-l");
			System.out.println(inStream.readObject());
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
