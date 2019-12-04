package ulisboa.tecnico.sirs.server.pep;

import java.io.IOException;

import org.ow2.authzforce.core.pdp.impl.BasePdpEngine;
import org.ow2.authzforce.core.pdp.impl.PdpEngineConfiguration;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

import org.ow2.authzforce.core.pdp.api.DecisionRequest;
import org.ow2.authzforce.core.pdp.api.DecisionRequestBuilder;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.ImmutableDecisionRequest;

public class PolicyEnforcementPoint
{
	private final PdpEngineConfiguration pdpEngineConf; 
	private final BasePdpEngine pdp;

	public PolicyEnforcementPoint() throws IllegalArgumentException, IOException {
		this.pdpEngineConf = PdpEngineConfiguration.getInstance("resources/pdpconfig.xml");
		this.pdp = new BasePdpEngine(pdpEngineConf);
	}
	
	private boolean authorize(String message) {
		// Create the request to be made to the PDP
		final DecisionRequest request = createRequest(message);
		// Evaluate the request
		final DecisionResult result = pdp.evaluate(request);

		return result.getDecision() == DecisionType.PERMIT;
	}
	
	private DecisionRequest createRequest(String message) {
		// Parse the message request
		ParsedMessage parsedMsg = new ParsedMessage(message);
		// Get the e-mail of the issuer of this request
		String userEmail = parsedMsg.getUserEmail();
		
		DecisionRequestBuilder<?> requestBuilder = pdp.newRequestBuilder(1,1);
		
		return requestBuilder.build(false);
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