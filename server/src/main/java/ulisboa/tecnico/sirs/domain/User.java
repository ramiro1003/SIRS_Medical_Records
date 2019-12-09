package ulisboa.tecnico.sirs.domain;

import java.sql.Date;

public class User {
	
	private Integer userId;
	private String name;
	private String type;
	private Date birthDate;

	public User(Integer userId, String name, String type, Date birthDate) {
		this.userId = userId;
		this.name = name;
		this.type = type;
		this.birthDate = birthDate;
	}
	
	public Integer getUserId() {
		return userId;
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
