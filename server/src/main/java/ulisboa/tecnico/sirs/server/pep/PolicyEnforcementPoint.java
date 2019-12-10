package ulisboa.tecnico.sirs.server.pep;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.casbin.jcasbin.main.Enforcer;

import ulisboa.tecnico.sirs.domain.MedicalRecord;
import ulisboa.tecnico.sirs.domain.User;

public class PolicyEnforcementPoint
{
	private final Enforcer enforcer;

	public PolicyEnforcementPoint() throws IllegalArgumentException, IOException {
		
		Path currentWorkingDirectory = FileSystems.getDefault().getPath("").toAbsolutePath();
		this.enforcer = new Enforcer(currentWorkingDirectory + "/resources/abac_model.conf");
	}
	
	public boolean enforce(User user, MedicalRecord patientId, String action, String context) {
		System.out.println("User " + user.getName() + " PatientId " + patientId + "Action " +  action + "Context " + context);
		Boolean authorization = this.enforcer.enforce(user, patientId, action, context);
		return authorization;
	}
		
}

class ParsedMessage {
	
	private String userEmail = null;
	
	public ParsedMessage(String message) {
		userEmail = message;
	}
	
	public String getUserEmail() {
		return userEmail;
	}
	
}