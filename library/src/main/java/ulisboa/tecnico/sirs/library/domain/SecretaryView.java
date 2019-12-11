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
							+ "1) Register User\n"
							+ "9) Change Your Password\n"
							+ "0) Quit\n"
							+ ">> ");

			userInput = scanner.nextLine().split(" ")[0];
			
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
			String typeOption = scanner.nextLine().split(" ")[0];
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
		// Variable used to sanitize user input
		Boolean goodInput = false;
		// Ask user name
		System.out.print("What's the user name?\n>> ");
		String username = scanner.nextLine();
		// Ask user citizen Id
		String userId = "";
		while(!goodInput) {
			System.out.print("What's the user Id?\n>> ");
			userId = scanner.nextLine();
			if(!userId.matches("^[0-9]{1,9}$")) { 
				System.out.println("Wrong Id format! Id is a number with 9 digits maximum.");
			}
			else {
				goodInput = true;
			}
		}
		goodInput = false;
		// Ask user birth date
		String year = "";
		String month = "";
		String day = "";
		System.out.println("When was the user born?");
		while(!goodInput) {
			System.out.print("Year: ");
			year = scanner.nextLine();
			if(!year.matches("^[0-9]{4}$")) { 
				System.out.println("Wrong year format. Year must be a number like XXXX");
			}
			else {
				goodInput = true;
			}
		}
		goodInput = false;
		while(!goodInput) {
			System.out.print("Month: ");
			month = scanner.nextLine();
			if(!month.matches("^(0?[1-9]|1[012])$")) { 
				System.out.println("Wrong month format. Year must be a number like X or XX");
			}
			else {
				if(month.length() == 1) {month = "0" + month;}
				goodInput = true;
			}
		}
		goodInput = false;
		while(!goodInput) {
			System.out.print("Day: ");
			day = scanner.nextLine();
			if(!day.matches("^(0?[1-9]|1[0-9]|2[0-9]|30|31)$")) { 
				System.out.println("Wrong day format. Year must be a number like X or XX");
			}
			else {
				if(day.length() == 1) {day = "0" + day;}
				goodInput = true;
			}
		}
		String birthDate = year + month + day;
		// Send registration request
		try {
			outStream.writeObject("-registerUser");
			// Send user Id
			outStream.writeObject(userId);
			if(inStream.readObject().equals("New user")) {
				// Generate password
				String password = generateRandomPassword();
				System.out.println("This is the user's initial password. This should be changed by the user: " + password);
				// Send username
				outStream.writeObject(username);
				// Send user type
				outStream.writeObject(type);
				// Send user birthdate
				outStream.writeObject(birthDate);
				// Send plain text password
				outStream.writeObject(password);
				System.out.println("User successfully registered in SIRS Medical Records Systems.");
			}
			else {
				System.out.println("User with such an e-mail already exists");
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

}
