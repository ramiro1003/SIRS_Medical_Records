package ulisboa.tecnico.sirs.library.domain;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Scanner;

public class PatientView extends UserView {
	
	private static final long serialVersionUID = 1L;

	public PatientView(String email, String name) {
		super(email, name);
	}

	@Override
	public void runApp(ObjectInputStream appInStream, ObjectOutputStream appOutStream, Scanner appScanner) {
		// TODO Auto-generated method stub
		
	}

}
