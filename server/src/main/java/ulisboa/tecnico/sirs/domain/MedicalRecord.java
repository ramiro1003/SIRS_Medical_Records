package ulisboa.tecnico.sirs.domain;

import java.util.List;

public class MedicalRecord {
	
	private Integer recordId, patientId, doctorId;
	private String name;
	private Integer height, weight;
	private List<Prescription> prescription;
	private List<Diagnosis> diagnosis;
	private List<Treatment> treatment;
	
	
	public MedicalRecord(Integer recordId, Integer patientId, Integer doctorId, String name, Integer height, Integer weight,
			List<Prescription> prescription, List<Diagnosis> diagnosis, List<Treatment> treatment) {
		this.recordId = recordId;
		this.patientId = patientId;
		this.doctorId = doctorId;
		this.name = name;
		this.height = height;
		this.weight = weight;
		this.prescription = prescription;
		this.diagnosis = diagnosis;
		this.treatment = treatment;
	}
	

	public Integer getRecordId() {
		return recordId;
	}


	public Integer getPatientId() {
		return patientId;
	}


	public Integer getDoctorId() {
		return doctorId;
	}

	
	public String getName() {
		return name;
	}

	
	public Integer getHeight() {
		return height;
	}


	public Integer getWeight() {
		return weight;
	}


	public List<Prescription> getPrescription() {
		return prescription;
	}


	public List<Diagnosis> getDiagnosis() {
		return diagnosis;
	}


	public List<Treatment> getTreatment() {
		return treatment;
	}
	
	
	
}
