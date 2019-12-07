package ulisboa.tecnico.sirs.library.domain;

import java.io.Serializable;

public class DoctorView extends UserView implements Serializable{

	private static final long serialVersionUID = 1L;

	public DoctorView(String email, String name) {
		super(email, name);
	}

}
