package ulisboa.tecnico.sirs.domain;

import java.sql.Date;

public class Medication {
	
	private int id;
	private int medicalRecordId;
	private String name;
	private Date prescriptionDate;
	
	public Medication(int id, int medicalRecordId, String name, Date prescriptionDate) {
		this.id = id;
		this.name = name;
		this.medicalRecordId = medicalRecordId;
		this.prescriptionDate = prescriptionDate;
	}

	public int getId() {
		return id;
	}

	public int getMedicalRecordId() {
		return medicalRecordId;
	}

	public String getName() {
		return name;
	}

	public Date getPrescriptionDate() {
		return prescriptionDate;
	}

}
