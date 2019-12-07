package ulisboa.tecnico.sirs.domain;

import java.util.ArrayList;
import java.util.List;

public class MedicalRecord {
	
	private User patient;
	private User doctor;
	private List<String> history;
	
	// FIXME DB format
	private Integer recordId, userId;
	private String patientName;
	private Integer age, height, weight;
	 
	public MedicalRecord(Integer recordId, Integer userId, String patientName, Integer age, Integer height, Integer weight) {
		this.recordId = recordId;
		this.userId = userId;
		this.patientName = patientName;
		this.age = age;
		this.height = height;
		this.weight = weight;	
	}
	
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
	
	
	public Integer getRecordId() {
		return recordId;
	}

	public Integer getUserId() {
		return userId;
	}

	public String getPatientName() {
		return patientName;
	}

	public Integer getAge() {
		return age;
	}

	public Integer getHeight() {
		return height;
	}

	public Integer getWeight() {
		return weight;
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
