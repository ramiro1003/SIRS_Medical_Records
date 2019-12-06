package ulisboa.tecnico.sirs.server.pep;

import static org.ow2.authzforce.xacml.identifiers.XacmlAttributeCategory.XACML_1_0_ACCESS_SUBJECT;

import static org.ow2.authzforce.xacml.identifiers.XacmlAttributeCategory.XACML_3_0_ACTION;
import static org.ow2.authzforce.xacml.identifiers.XacmlAttributeCategory.XACML_3_0_RESOURCE;

import java.io.IOException;
import java.util.Optional;

import org.ow2.authzforce.core.pdp.impl.BasePdpEngine;
import org.ow2.authzforce.core.pdp.impl.PdpEngineConfiguration;
import org.ow2.authzforce.xacml.identifiers.XacmlAttributeId;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

import org.casbin.jcasbin.main.Enforcer;
/*import org.ow2.authzforce.core.pdp.api.AttributeFqn;
import org.ow2.authzforce.core.pdp.api.AttributeFqns;
import org.ow2.authzforce.core.pdp.api.DecisionRequest;
import org.ow2.authzforce.core.pdp.api.DecisionRequestBuilder;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.ImmutableDecisionRequest;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.springframework.core.io.ClassPathResource;*/

public class PolicyEnforcementPoint
{
	//private final PdpEngineConfiguration pdpEngineConf; 
	//private final BasePdpEngine pdp;
	private final Enforcer enforcer;

	public PolicyEnforcementPoint() throws IllegalArgumentException, IOException {
		
		//ClassPathResource url = new ClassPathResource("data/employees.dat", this.getClass().getClassLoader());
		
		//this.pdpEngineConf = PdpEngineConfiguration.getInstance(url.getFile().getAbsolutePath());
		
		//this.pdpEngineConf = PdpEngineConfiguration.getInstance("pdpconfig.xml", "pdp");
		
		//this.pdp = new BasePdpEngine(pdpEngineConf);
		
		this.enforcer = new Enforcer("abac_model.conf");
		
	}
	
	
	public boolean enforce(String action, String subjectId, String subjectRole, String patientId, Boolean emergencyContext) {
		return this.enforcer.enforce(subjectId, patientId, action);
	}
	
	
	
	/*
	public boolean authorize(String action, String subjectId, String subjectRole, String patientId, Boolean emergencyContext) {
		// Create the request to be made to the PDP
		final DecisionRequest request = createRequest(action, subjectId, subjectRole, patientId, emergencyContext);
		// Evaluate the request
		final DecisionResult result = pdp.evaluate(request);

		return result.getDecision() == DecisionType.PERMIT;
	}
	
	private DecisionRequest createRequest(String action, String subjectId, String subjectRole, String recordId, Boolean emergencyContext) {
		
		DecisionRequestBuilder<?> requestBuilder = pdp.newRequestBuilder(1,1);
				
		addSubjectId(subjectId, requestBuilder);
		addAction(action, requestBuilder);
		addSubjectRole(subjectRole, requestBuilder);
		//addAssignedDoctorId(assignedDoctorId, requestBuilder);
		//addAssignedStaffId(assignedStaffId, requestBuilder);
		addEmergencyContext(emergencyContext, requestBuilder);
		
		return requestBuilder.build(false);
	}
	
	
	// CHECK NULLS	
	
	private void addSubjectRole(String attribute, DecisionRequestBuilder<?> requestBuilder) {
			final AttributeFqn subjectRoleAttributeId = AttributeFqns.newInstance(XACML_1_0_ACCESS_SUBJECT.value(), null, XacmlAttributeId.XACML_2_0_SUBJECT_ROLE.value());
			final AttributeBag<?> subjectRoleAttributeValues = Bags.singletonAttributeBag(StandardDatatypes.STRING, new StringValue(attribute));
			requestBuilder.putNamedAttributeIfAbsent(subjectRoleAttributeId, subjectRoleAttributeValues);
	}
	
	private void addAction(String attribute, DecisionRequestBuilder<?> requestBuilder) {
		final AttributeFqn actionAttributeId = AttributeFqns.newInstance(XACML_1_0_ACCESS_SUBJECT.value(), null, XacmlAttributeId.XACML_2_0_SUBJECT_ROLE.value());
		final AttributeBag<?> actionAttributeValues = Bags.singletonAttributeBag(StandardDatatypes.STRING, new StringValue(attribute));
		requestBuilder.putNamedAttributeIfAbsent(actionAttributeId, actionAttributeValues);
	}
	
	private void addSubjectId(String attribute, DecisionRequestBuilder<?> requestBuilder) {
		final AttributeFqn subjectIdAttributeId = AttributeFqns.newInstance(XACML_1_0_ACCESS_SUBJECT.value(), null, XacmlAttributeId.XACML_2_0_SUBJECT_ROLE.value());
		final AttributeBag<?> subjectIdAttributeValues = Bags.singletonAttributeBag(StandardDatatypes.STRING, new StringValue(attribute));
		requestBuilder.putNamedAttributeIfAbsent(subjectIdAttributeId, subjectIdAttributeValues);
	}
	
	private void addAssignedDoctorId(String attribute, DecisionRequestBuilder<?> requestBuilder) {
		final AttributeFqn assignedDoctorIdAttributeId = AttributeFqns.newInstance(XACML_1_0_ACCESS_SUBJECT.value(), null, XacmlAttributeId.XACML_2_0_SUBJECT_ROLE.value());
		final AttributeBag<?> assignedDoctorIdAttributeValues = Bags.singletonAttributeBag(StandardDatatypes.STRING, new StringValue(attribute));
		requestBuilder.putNamedAttributeIfAbsent(assignedDoctorIdAttributeId, assignedDoctorIdAttributeValues);
	}
	
	private void addAssignedStaffId(String attribute, DecisionRequestBuilder<?> requestBuilder) {
		final AttributeFqn assignedStaffIdAttributeId = AttributeFqns.newInstance(XACML_1_0_ACCESS_SUBJECT.value(), null, XacmlAttributeId.XACML_2_0_SUBJECT_ROLE.value());
		final AttributeBag<?> assignedStaffIdAttributeValues = Bags.singletonAttributeBag(StandardDatatypes.STRING, new StringValue(attribute));
		requestBuilder.putNamedAttributeIfAbsent(assignedStaffIdAttributeId, assignedStaffIdAttributeValues);
	}
	
	private void addEmergencyContext(Boolean attribute, DecisionRequestBuilder<?> requestBuilder) {
		final AttributeFqn emergencyContextAttributeId = AttributeFqns.newInstance(XACML_1_0_ACCESS_SUBJECT.value(), null, XacmlAttributeId.XACML_2_0_SUBJECT_ROLE.value());
		final AttributeBag<?> emergencyContextAttributeValues = Bags.singletonAttributeBag(StandardDatatypes.BOOLEAN, new BooleanValue(attribute));
		requestBuilder.putNamedAttributeIfAbsent(emergencyContextAttributeId, emergencyContextAttributeValues);
	}
	*/
		
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