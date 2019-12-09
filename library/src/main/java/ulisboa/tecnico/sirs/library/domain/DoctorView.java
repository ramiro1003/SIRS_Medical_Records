package ulisboa.tecnico.sirs.library.domain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Scanner;

public class DoctorView extends UserView{

	private static final long serialVersionUID = 1L;
	private static Scanner scanner;
	private static ObjectInputStream inStream;
	private static ObjectOutputStream outStream;

	public DoctorView(Integer userId, String email, String name) {
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
							+ "1) List Patients\n"
							+ "2) Read a Medical Record\n"
							+ "3) Edit a Medical Record\n"
							+ "9) Change Password\n"
							+ "0) Quit\n"
							+ ">> ");

			userInput = scanner.nextLine().split(" ")[0]; // FIXME NOT SANITIZING USER INPUT
			
			switch(userInput) {
				case "1":
					listPatients();
					break;
				case "2":
					readMedicalRecord();
					break;
				case "3":
					writeMedicalRecord();
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

	@SuppressWarnings("unchecked")
	private void listPatients() {
		try {
			outStream.writeObject("-listP");
			String answer = (String) inStream.readObject();
			if(answer.equals("Doctor has patients")) {
				List<PatientView> patients = (List<PatientView>) inStream.readObject();
				for(PatientView p : patients) {
					p.printInfo();
				}
			}
			else {
				System.out.println("This doctor has no patients assigned to him");
			}
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	private void writeMedicalRecord() {
		try {
			outStream.writeObject("-writeMD");
			System.out.print("What's the Patient's Id?\n>> ");
			String patientId = scanner.nextLine().split(" ")[0]; //FIXME NOT SANITIZING USER INPUT
			outStream.writeObject(patientId);
			String access = (String) inStream.readObject();
			
			if(access.equals("Authorized")) {
				
				Boolean quitWrite = false;
				String writeAction;
				
				while(!quitWrite) {
					System.out.print("Please choose the number of what you want to wish to write:\n"
							+ "1) Change height\n"
							+ "2) Change weight\n"
							+ "3) Add prescription\n"
							+ "4) Add diagnosis\n"
							+ "5) Add treatment\n"
							+ "0) Back\n"
							+ ">> ");
					
					writeAction = scanner.nextLine().split(" ")[0]; // FIXME NOT SANITIZING USER INPUT
					
					switch(writeAction) {
						case "1":
							changeHeight();
							break;
						case "2":
							changeWeight();
							break;
						case "3":
							addPrescription();
							break;
						case "4":
							addDiagnosis();
							break;
						case "5":
							addTreatment();
							break;
						case "0":
							quitWrite();
							quitWrite = true;
							break;
						default:
							System.out.println("Invalid instruction!");
					}
				}
			}
			else {
				System.out.println("You don't have write access to this Medical Record");
			}
			
			
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
	}
	
	
	
	private void changeHeight() {
		try {
			outStream.writeObject("-changeHeight");
			System.out.print("What is the patient's height? (centimeters) \n>> ");
			String heightStr = scanner.nextLine(); //FIXME NOT SANITIZING USER INPUT
			
			
			if(!heightStr.matches("^[0-9]{2,3}$")) { 
				System.out.println("Wrong height format!");
				return;
			}
			
			outStream.writeObject(heightStr);
			
			String complete = (String) inStream.readObject();
			if(complete.contentEquals("complete"))
				System.out.print("Patient height updated.");
			return;
			
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	private void changeWeight() {
		try {
			outStream.writeObject("-changeWeight");
			System.out.print("What is the patient's weight? (kilograms) \n>> ");
			String weightStr = scanner.nextLine(); //FIXME NOT SANITIZING USER INPUT
			
			if(!weightStr.matches("^[0-9]{1,3}$")) { 
				System.out.println("Wrong weight format!");
				return;
			}
			
			outStream.writeObject(weightStr);
			
			String complete = (String) inStream.readObject();
			if(complete.contentEquals("complete"))
				System.out.print("Patient weight updated.");
			return;
			
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
	}
	
	private void addPrescription() {
		try {
			outStream.writeObject("-addPrescription");
			System.out.print("Add new patient prescription:\n>> ");
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
	}

	private void addDiagnosis() {
		try {
			outStream.writeObject("-addDiagnosis");
			System.out.print("Add new patient diagnosis:\n>> ");
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
	}
	
	private void addTreatment() {
		try {
			outStream.writeObject("-addTreatment");
			System.out.print("Add new patient treatment:\n>> ");
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	private void quitWrite() {
		try {
			outStream.writeObject("-quitWrite");
		} catch (IOException e) {
			e.printStackTrace();
		}			
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
								+ "  - Can't contain any name of yours\n"
								+ "  - Can't contain words that appear in your email");
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
