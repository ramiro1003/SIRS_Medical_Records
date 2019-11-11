package ulisboa.tecnico.sirs.server.pep;

import java.io.IOException;

import org.ow2.authzforce.core.pdp.impl.BasePdpEngine;
import org.ow2.authzforce.core.pdp.impl.PdpEngineConfiguration;

public class PolicyEnforcementPoint
{
	private final PdpEngineConfiguration pdpEngineConf; 
	private final BasePdpEngine pdp;

	public PolicyEnforcementPoint() throws IllegalArgumentException, IOException {
		this.pdpEngineConf = PdpEngineConfiguration.getInstance("pdpconfig.xml");
		this.pdp = new BasePdpEngine(pdpEngineConf);
	}
}