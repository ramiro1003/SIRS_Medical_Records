package ulisboa.tecnico.sirs.library.domain;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Scanner;

public abstract class UserView implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String userId;
	private String email;
	private String name;

	public UserView(String userId, String email, String name) {
		this.userId = userId;
		this.email = email;
		this.name = name;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public String getEmail() {
		return email;
	}

	public String getName() {
		return name;
	}
	
	public abstract void runApp(ObjectInputStream appInStream, ObjectOutputStream appOutStream, Scanner appScanner);
	
}
