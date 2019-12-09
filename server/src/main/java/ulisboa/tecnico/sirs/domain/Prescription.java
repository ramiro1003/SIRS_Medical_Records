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

}
