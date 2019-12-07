package ulisboa.tecnico.sirs.client;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Scanner;

import javax.net.ssl.SSLSocket;

import ulisboa.tecnico.sirs.library.domain.*;

public class AppDoctor {
	
	
	private static Scanner scanner;
	private static DoctorView user;
	private static ObjectInputStream inStream;
	private static ObjectOutputStream outStream;
	
	public static void runApp(ObjectInputStream appInStream, ObjectOutputStream appOutStream, Scanner appScanner, DoctorView doctorView) {
		// Get socket from 'main App'
		inStream = appInStream;
		outStream = appOutStream;
		scanner = appScanner;
		user = doctorView;
		// Variable used to store user input
		String userInput;
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
			System.out.print("What's the Patient's Id?\n>> ");
			String patientId = scanner.nextLine().split(" ")[0]; //FIXME NOT SANITIZING USER INPUT
			outStream.writeObject(patientId);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void quitClient() {
		try {
			outStream.writeObject("-quit");
			outStream.writeObject(user.getName());
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

}
