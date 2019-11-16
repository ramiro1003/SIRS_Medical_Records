CREATE OR REPLACE TABLE User (
	Id INT NOT NULL AUTO_INCREMENT,
	Name VARCHAR(150),
	Type VARCHAR(100),
	PRIMARY KEY (Id),
	CONSTRAINT chk_Type CHECK (Type IN ('Patient', 'Doctor', 'Staff', 'Admin'))
);

CREATE OR REPLACE TABLE MedicalRecord (
	Id INT NOT NULL AUTO_INCREMENT,
	PRIMARY KEY (Id)
);

INSERT INTO User (Name, Type)
VALUES ('João', 'Patient');

INSERT INTO User (Name, Type)
VALUES ('Miguel', 'Patient');
