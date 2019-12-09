package ulisboa.tecnico.sirs.library.domain;

import java.util.List;

public class MedicalRecordView {
	
	private Integer recordId, patientId, doctorId;
	private String name;
	private Integer height, weight;
	private List<List<String>> prescriptionsInfo;
	private List<List<String>> diagnosesInfo;
	private List<List<String>> treatmentsInfo;
	
	
	public MedicalRecordView(Integer recordId, Integer patientId, Integer doctorId, String name, Integer height, Integer weight,
			List<List<String>> prescriptions, List<List<String>> diagnoses, List<List<String>> treatments) {
		this.recordId = recordId;
		this.patientId = patientId;
		this.doctorId = doctorId;
		this.name = name;
		this.height = height;
		this.weight = weight;
		this.prescriptionsInfo = prescriptions;
		this.diagnosesInfo = diagnoses;
		this.treatmentsInfo = treatments;
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


	public List<List<String>> getPrescriptionsInfo() {
		return prescriptionsInfo;
	}


	public List<List<String>> getDiagnosesInfo() {
		return diagnosesInfo;
	}


	public List<List<String>> getTreatmentsInfo() {
		return treatmentsInfo;
	}


	public char[] getInfo() {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
