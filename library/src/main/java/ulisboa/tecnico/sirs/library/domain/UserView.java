package ulisboa.tecnico.sirs.library.domain;

import java.io.Serializable;

public class UserView implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String email;
	private String name;

	public UserView(String email, String name) {
		this.email = email;
		this.name = name;
	}
	
	public String getEmail() {
		return email;
	}

	public String getName() {
		return name;
	}
	

}
