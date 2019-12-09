package ulisboa.tecnico.sirs.library.domain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.SecureRandom;
import java.util.Scanner;

public class SecretaryView extends UserView {
	
	private static final long serialVersionUID = 1L;
	private static Scanner scanner;
	private static ObjectInputStream inStream;
	private static ObjectOutputStream outStream;
	
	public SecretaryView(Integer userId, String name) {
		super(userId, name);
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
							+ "1) Register User\n"
							+ "9) Change Your Password\n"
							+ "0) Quit\n"
							+ ">> ");

			userInput = scanner.nextLine().split(" ")[0]; // FIXME NOT SANITIZING USER INPUT
			
			switch(userInput) {
			case "1":
				registerUser();
				break;
			case "9":
				changePassword();
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
	
	private void registerUser() {
		// Pedir o tipo de utilizador
		String type = null;
		Boolean exitSwitch = false;
		while(!exitSwitch) {
			System.out.print("Are you registering a new doctor, patient, staff or secretary?\n"
							+ "1) Doctor\n"
							+ "2) Patient\n"
							+ "3) Staff\n"
							+ "4) Secretary\n"
							+ ">> ");
			String typeOption = scanner.nextLine().split(" ")[0]; //FIXME NOT SANITIZING USER INPUT
			switch(typeOption) {
			case "1":
				type = "Doctor";
				exitSwitch = true;
				break;
			case "2":
				type = "Patient";
				exitSwitch = true;
				break;
			case "3":
				type = "Staff";
				exitSwitch = true;
				break;
			case "4":
				type = "Secretary";
				exitSwitch = true;
				break;
			default:
				System.out.println("Invalid instruction!");
			}
		}
		// Ask user name
		System.out.print("What's the user name?\n>> ");
		String username = scanner.nextLine(); //FIXME NOT SANITIZING USER INPUT
		// Ask user citizen Id
		System.out.print("What's the user Id?\n>> ");
		String userId = scanner.nextLine(); //FIXME NOT SANITIZING USER INPUT
		// Send registration request
		try {
			outStream.writeObject("-registerUser");
			// Send user Id
			outStream.writeObject(userId);
			if(inStream.readObject().equals("New user")) {
				// Generate password
				String password = generateRandomPassword();
				System.out.println("This is the user initial password. This should be changed by the user:  " + password);
				// Send username
				outStream.writeObject(username);
				// Send user type
				outStream.writeObject(type);
				// Send plain text password
				outStream.writeObject(password);
				System.out.println("User successfully registered in SIRS Medical Records Systems.");
			}
			else {
				System.out.println("User with such e-mail already exists");
			}
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	private String generateRandomPassword() {
		//FIXME
		SecureRandom random = new SecureRandom();
		String dictionary = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%_+-";
		String password = "";
		for (int i = 0; i < 12; i++) {
		    int index = random.nextInt(dictionary.length());
		    password += dictionary.charAt(index);
		}
		return password;
	}

	private void changePassword() {
		try {
			// Get user old password so he can authenticate himself
			System.out.print("Enter your current password:\n>> ");
			String password = scanner.nextLine(); //FIXME NOT SANITIZING USER INPUT
			outStream.writeObject(password);
			if(inStream.readObject().equals("User authenticated")) {
				// Ask user for new password and confirm it
				System.out.print("Enter your new password:\n>> ");
				String newPass = scanner.nextLine(); //FIXME NOT SANITIZING USER INPUT
				System.out.print("Confirm your new password by reentering it:\n>> ");
				String confirmNewPass = scanner.nextLine(); //FIXME NOT SANITIZING USER INPUT
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

}
