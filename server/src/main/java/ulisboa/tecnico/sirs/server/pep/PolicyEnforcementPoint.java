package ulisboa.tecnico.sirs.server.pep;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.casbin.jcasbin.main.Enforcer;

import ulisboa.tecnico.sirs.domain.User;

public class PolicyEnforcementPoint
{
	//private final PdpEngineConfiguration pdpEngineConf; 
	//private final BasePdpEngine pdp;
	private final Enforcer enforcer;

	public PolicyEnforcementPoint() throws IllegalArgumentException, IOException {
		
		Path currentWorkingDirectory = FileSystems.getDefault().getPath("").toAbsolutePath();
		this.enforcer = new Enforcer(currentWorkingDirectory + "/resources/abac_model.conf", currentWorkingDirectory + "/resources/policy.csv");
	}
	
	
	public boolean enforce(User user, String resourceId, String action) {
		
		Boolean authorization = this.enforcer.enforce(user, resourceId, action);
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