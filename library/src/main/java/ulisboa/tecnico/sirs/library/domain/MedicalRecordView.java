package ulisboa.tecnico.sirs.library.domain;

import java.io.Serializable;
import java.util.List;

public class MedicalRecordView implements Serializable {
	
	private static final long serialVersionUID = 1L;
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


	public String getInfo() {
		String info = "";
		List<List<String>> prescriptionsInfo = this.getPrescriptionsInfo();
		List<List<String>> diagnosesInfo = this.getDiagnosesInfo();
		List<List<String>> treatmentsInfo = this.getTreatmentsInfo();
		info += "This Medical Record belongs to " + this.getName() + " with Id " + this.getPatientId() + ".\n";
		info += "Patient's info:\n  - Height: " + this.getHeight() + "cm\n  - Weight: " + this.getWeight() + "kg\n\n";
		
		if(diagnosesInfo.size() != 0) {
			info += "Diagnoses:\n";
			for(List<String> diagnosis : diagnosesInfo) {
				info += "  - " + diagnosis.get(0) + " diagnosed at " + diagnosis.get(1) +".\n";
				if(diagnosis.get(2) != null) {
					info += "\tDescription: " + diagnosis.get(2) + "\n";
				}
			}
			info += "\n";
		}
		
		if(prescriptionsInfo.size() != 0) {
			info += "Prescriptions:\n";
			for(List<String> prescription : prescriptionsInfo) {
				info += "  - " + prescription.get(0) + " prescribed at " + prescription.get(1) +".\n";
			}
			info += "\n";
		}
		
		if(treatmentsInfo.size() != 0) {
			info += "Treatments:\n";
			for(List<String> treatments : treatmentsInfo) {
				info += "  - " + treatments.get(0) + " done at " + treatments.get(1) +".\n";
				if(treatments.get(2) != null) {
					info += "\tDescription: " + treatments.get(2) + "\n";
				}
			}
			info += "\n";
		}
		
		return info;
	}
	
	

}
