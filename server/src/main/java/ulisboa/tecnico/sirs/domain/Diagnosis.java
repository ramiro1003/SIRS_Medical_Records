package ulisboa.tecnico.sirs.domain;

import java.sql.Date;

public class Diagnosis {
	
	private String name;
	private Date diagnosisDate;
	private String description;
	
	public Diagnosis(String name, Date diagnosisDate, String description) {
		this.name = name;
		this.diagnosisDate = diagnosisDate;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public Date getDiagnosisDate() {
		return diagnosisDate;
	}

	public String getDescription() {
		return description;
	}
		

}
