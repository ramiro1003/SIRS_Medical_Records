package ulisboa.tecnico.sirs.domain;

import java.sql.Date;

public class Treatment {

	private String name;
	private Date treatmentDate;
	private String description;
	
	public Treatment(String name, Date treatmentDate, String description) {
		this.name = name;
		this.treatmentDate = treatmentDate;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public Date getTreatmentDate() {
		return treatmentDate;
	}

	public String getDescription() {
		return description;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("Treatment: ");
		sb.append("| Name: " + name);
		sb.append(" | Description: " + description);
		sb.append(" | Date: " + treatmentDate.toString() + " |");
		return sb.toString();
	}
	
}
