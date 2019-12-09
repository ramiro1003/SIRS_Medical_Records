package ulisboa.tecnico.sirs.library.domain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Scanner;

public class StaffView extends UserView {

	private static final long serialVersionUID = 1L;
	private static Scanner scanner;
	private static ObjectInputStream inStream;
	private static ObjectOutputStream outStream;

	public StaffView(Integer userId, String email, String name) {
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
							+ "1) Read a Medical Record\n"
							+ "0) Quit\n"
							+ ">> ");

			userInput = scanner.nextLine().split(" ")[0]; // FIXME NOT SANITIZING USER INPUT
			
			switch(userInput) {
			case "1":
				readMedicalRecord();
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
	
	private void readMedicalRecord() {
		try {
			outStream.writeObject("-readMD");
			System.out.print("What's the Patient's Id?\n>> ");
			String patientId = scanner.nextLine().split(" ")[0]; //FIXME NOT SANITIZING USER INPUT
			outStream.writeObject(patientId);
			String access = (String) inStream.readObject();
			if(access.equals("Authorized")) {
				MedicalRecordView medicalRecordView = (MedicalRecordView) inStream.readObject();
				System.out.println(medicalRecordView.getInfo());
			}
			else {
				System.out.println("You don't have access to this Medical Record");
			}
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
