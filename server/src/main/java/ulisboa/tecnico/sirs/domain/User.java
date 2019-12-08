package ulisboa.tecnico.sirs.domain;

import java.sql.Date;

public class User {
	
	private int userId;
	private String email;
	private String name;
	private String type;
	private Date birthDate;

	public User(int userId2, String email, String name, String type, Date birthDate) {
		this.userId = userId2;
		this.email = email;
		this.name = name;
		this.type = type;
		this.birthDate = birthDate;
	}
	
	public int getUserId() {
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
	
	public Date getDate() {
		return birthDate;
	}
}
