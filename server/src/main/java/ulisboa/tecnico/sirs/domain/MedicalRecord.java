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
			prescription.add(p.getName());
			prescription.add( p.getPrescriptionDate().toString());
		}
		return info;
	}
	
	public List<List<String>> getDiagnosesInfo() {
		List<List<String>> info = new ArrayList<>();
		for(Diagnosis d : this.getDiagnoses()) {
			List<String> diagnosis = new ArrayList<>();
			diagnosis.add(d.getName());
			diagnosis.add(d.getDiagnosisDate().toString());
			diagnosis.add(d.getDescription());
		}
		return info;
	}
	
	public List<List<String>> getTreatmentsInfo() {
		List<List<String>> info = new ArrayList<>();
		for(Treatment t : this.getTreatments()) {
			List<String> treatment = new ArrayList<>();
			treatment.add(t.getName());
			treatment.add(t.getTreatmentDate().toString());
			treatment.add(t.getDescription());
		}
		return info;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("Medical Record no" + recordId + "\n");
		sb.append("Patient id: " + patientId + "\n");
		sb.append("Patient name: " + name + "\n");
		sb.append("Patient weight: " + weight + "\n");
		sb.append("Patient height: " + height + "\n");
		for(Prescription p : prescription) {
			sb.append(p.toString() + "\n");
		}
		for(Diagnosis d : diagnosis) {
			sb.append(d.toString() + "\n");
		}
		for(Treatment t : treatment) {
			sb.append(t.toString() + "\n");
		}
		return sb.toString();
	}
	
}
