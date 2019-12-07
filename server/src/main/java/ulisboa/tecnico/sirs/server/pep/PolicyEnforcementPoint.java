package ulisboa.tecnico.sirs.server.pep;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.casbin.jcasbin.main.Enforcer;

public class PolicyEnforcementPoint
{
	//private final PdpEngineConfiguration pdpEngineConf; 
	//private final BasePdpEngine pdp;
	private final Enforcer enforcer;

	public PolicyEnforcementPoint() throws IllegalArgumentException, IOException {
		
		Path currentWorkingDirectory = FileSystems.getDefault().getPath("").toAbsolutePath();
		System.out.println(currentWorkingDirectory);
		this.enforcer = new Enforcer(currentWorkingDirectory + "/resources/abac_model.conf", currentWorkingDirectory + "/resources/policy.csv");
		
	}
	
	
	public boolean enforce(String subjectId, String resourceId, String action) {
		
		Boolean authorization = this.enforcer.enforce(subjectId, resourceId, action);
		System.out.println(authorization);
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