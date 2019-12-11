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

	public PatientView(Integer userId, String name) {
		super(userId, name);
	}

	@Override
	public void runApp(ObjectInputStream appInStream, ObjectOutputStream appOutStream, Scanner appScanner, String context) {
		// Get socket from 'main App'
		inStream = appInStream;
		outStream = appOutStream;
		scanner = appScanner;
		// User logged in, now running application
		Boolean quit = false;
		// Check context
		if(context.equals("operationroom")) {
			System.out.println("\nYou can't login from an Operation Room. Security will be informed of this behaviour.");
			quit = true;
			quitClient();
		}
		else if(context.equals("emergency")) {
			System.out.println("\nYou can't login from an Emergency Context. Security will be informed of this behaviour.");
			quit = true;
			quitClient();
		}
		else {
			// Welcome message
			System.out.println("\nHello " + this.getName() + "!");
		}
		// Variable used to store user input
		String userInput;
		while(!quit) {
			System.out.print("\nPlease choose the number of what you want to perform and press enter:\n"
							+ "1) Check my Medical Record\n"
							+ "9) Change Password\n"
							+ "0) Quit\n"
							+ ">> ");

			userInput = scanner.nextLine().split(" ")[0];
			
			switch(userInput) {
			case "1":
				readMDClient();
				break;
			case "9":
				changePassword();
				break;
			case "0":
				System.out.print("Sure you want to quit? (Y = Yes, N = No)\n>> ");
				String conf = scanner.nextLine().split(" ")[0];
				switch(conf) {
					case "Y":
					case "y":
					case "yes":
					case "Yes":
					case "YES":
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
			outStream.writeObject(this.getUserId().toString());
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
	
	private void changePassword() {
		try {
			outStream.writeObject("-changePassword");
			// Get user old password so he can authenticate himself
			System.out.print("Enter your current password:\n>> ");
			String password = scanner.nextLine();
			outStream.writeObject(password);
			if(inStream.readObject().equals("User authenticated")) {
				// Ask user for new password and confirm it
				System.out.print("Enter your new password:\n>> ");
				String newPass = scanner.nextLine();
				System.out.print("Confirm your new password by reentering it:\n>> ");
				String confirmNewPass = scanner.nextLine();
				if(newPass.equals(confirmNewPass)) {
					outStream.writeObject("Password confirmed");
					outStream.writeObject(newPass);
					// Checks if server accepts our password (strength-wise)
					String passStrength = (String) inStream.readObject();
					if(passStrength.equals("Strong password")) {
						System.out.println("Your password was successfully updated");
					}
					else {
						System.out.println("Your new password doesn't meet at least one of the following requirements:\n"
								+ "  - Must have a minimum of 12 characters\n"
								+ "  - Must contain at least 1 upper case letter\n"
								+ "  - Must contain at least 1 lower case letter\n"
								+ "  - Must contain at least 1 number\n"
								+ "  - Can't contain any name of yours");
					}
				}
				else {
					outStream.writeObject("Password not confirmed");
					System.out.println("Passwords don't match.");
				}
			}
			else {
				System.out.println("Wrong password");
			}
		} catch (ClassNotFoundException | IOException e) {
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
		System.out.println("\t" + this.getUserId() + "\t\t|\t" + this.getName());
	}

}
