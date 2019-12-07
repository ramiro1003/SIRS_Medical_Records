package ulisboa.tecnico.sirs.domain;


public class User {
	
	private String userId;
	private String email;
	private String name;
	private String type;

	public User(String userId, String email, String name, String type) {
		this.userId = userId;
		this.email = email;
		this.name = name;
		this.type = type;
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
	
	public String getType() {
		return type;
	}
}
