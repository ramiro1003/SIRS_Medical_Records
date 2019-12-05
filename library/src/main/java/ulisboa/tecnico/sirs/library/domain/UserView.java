package ulisboa.tecnico.sirs.library.domain;

public class UserView {
	
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
