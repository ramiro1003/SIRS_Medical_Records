package ulisboa.tecnico.sirs.library.domain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Scanner;

public class PatientView extends UserView {
	
	private static final long serialVersionUID = 1L;
	private static Scanner scanner;
	private static ObjectInputStream inStream;
	private static ObjectOutputStream outStream;

	public PatientView(Integer userId, String email, String name) {
		super(userId, email, name);
	}

	@Override
	public void runApp(ObjectInputStream appInStream, ObjectOutputStream appOutStream, Scanner appScanner) {
		// Get socket from 'main App'
		inStream = appInStream;
		outStream = appOutStream;
		scanner = appScanner;
		// Variable used to store user input
		String userInput;
		// User logged in, now running application
		Boolean quit = false;
		while(!quit) {
			System.out.print("Please choose the number of what you want to perform and press enter:\n"
							+ "1) Check my Medical Record\n"
							+ "0) Quit\n"
							+ ">> ");

			userInput = scanner.nextLine().split(" ")[0]; // FIXME NOT SANITIZING USER INPUT
			
			switch(userInput) {
			case "1":
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
	
	private void readMDClient() {
		try {
			outStream.writeObject("-readMD");
			String patientId = String.valueOf(this.getUserId());
			outStream.writeObject(patientId);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void quitClient() {
		try {
			outStream.writeObject("-quit");
			outStream.writeObject(this.getName());
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	public void printInfo() {
		System.out.println("Patient's Name: " + this.getName()
		+ "Patient's ID: " + this.getUserId());
	}

}
