package ulisboa.tecnico.sirs.domain;

import java.util.ArrayList;
import java.util.List;

public class MedicalRecord {
	
	private Patient patient;
	private Doctor doctor;
	private List<String> history;
	
	public MedicalRecord(Patient p, Doctor d, List<String> history) {
		this.patient = p;
		this.doctor = d;
		this.history = history;
	}
	
	public MedicalRecord(Patient p, Doctor d) {
		this.patient = p;
		this.doctor = d;
		this.history = new ArrayList<String>();
	}

	public Patient getPatient() {
		return patient;
	}

	public Doctor getDoctor() {
		return doctor;
	}

	public List<String> getHistory() {
		return history;
	}
}
