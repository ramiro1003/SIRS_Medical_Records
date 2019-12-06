package ulisboa.tecnico.sirs.domain;

import java.util.ArrayList;
import java.util.List;

public class MedicalRecord {
	
	private User patient;
	private User doctor;
	private List<String> history;
	
	public MedicalRecord(User patient, User doctor, List<String> history) {
		this.patient = patient;
		this.doctor = doctor;
		this.history = history;
	}
	
	public MedicalRecord(User patient, User doctor) {
		this.patient = patient;
		this.doctor = doctor;
		this.history = new ArrayList<String>();
	}

	public User getPatient() {
		return patient;
	}

	public User getDoctor() {
		return doctor;
	}

	public List<String> getHistory() {
		return history;
	}
}
