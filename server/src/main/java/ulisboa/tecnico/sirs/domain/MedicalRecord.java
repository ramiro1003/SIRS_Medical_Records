package ulisboa.tecnico.sirs.domain;

import java.util.ArrayList;
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


	public List<Prescription> getPrescriptions() {
		return prescription;
	}


	public List<Diagnosis> getDiagnoses() {
		return diagnosis;
	}


	public List<Treatment> getTreatments() {
		return treatment;
	}
	
	public List<List<String>> getPrescriptionsInfo() {
		List<List<String>> info = new ArrayList<>();
		for(Prescription p : this.getPrescriptions()) {
			List<String> prescription = new ArrayList<>();
			prescription.set(0, p.getName());
			prescription.set(1, p.getPrescriptionDate().toString());
		}
		return info;
	}
	
	public List<List<String>> getDiagnosesInfo() {
		List<List<String>> info = new ArrayList<>();
		for(Diagnosis d : this.getDiagnoses()) {
			List<String> diagnosis = new ArrayList<>();
			diagnosis.set(0, d.getName());
			diagnosis.set(1, d.getDiagnosisDate().toString());
			diagnosis.set(2, d.getDescription());
		}
		return info;
	}
	
	public List<List<String>> getTreatmentsInfo() {
		List<List<String>> info = new ArrayList<>();
		for(Treatment t : this.getTreatments()) {
			List<String> treatment = new ArrayList<>();
			treatment.set(0, t.getName());
			treatment.set(1, t.getTreatmentDate().toString());
			treatment.set(2, t.getDescription());
		}
		return info;
	}
	
}
