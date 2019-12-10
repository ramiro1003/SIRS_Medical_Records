package ulisboa.tecnico.sirs.domain;

import java.sql.Date;

public class Prescription {
	
	private String name;
	private Date prescriptionDate;
	
	public Prescription(String name, Date prescriptionDate) {
		this.name = name;
		this.prescriptionDate = prescriptionDate;
	}

	public String getName() {
		return name;
	}

	public Date getPrescriptionDate() {
		return prescriptionDate;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("Prescription: ");
		sb.append("| Name: " + name);
		sb.append("| Date: " + prescriptionDate.toString() + " |");
		return sb.toString();
	}

}
