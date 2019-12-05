DROP TABLE IF EXISTS Auth;
DROP TABLE IF EXISTS User;
DROP TABLE IF EXISTS MedicalRecord;


CREATE TABLE User (
	Email VARCHAR(150),
	Name VARCHAR(150),
	Type VARCHAR(100),
	PRIMARY KEY (Email),
	CONSTRAINT chk_Type CHECK (Type IN ('Patient', 'Doctor', 'Staff', 'Admin'))
);

CREATE TABLE MedicalRecord (
	Id INT NOT NULL AUTO_INCREMENT,
	PatientId INT,
	PatientName VARCHAR(150), 
	Age INT,
	Weight INT,
	Height INT,
	PRIMARY KEY (Id)
);


CREATE TABLE Auth (
	Email VARCHAR(150),
	HashedPass VARCHAR(64),
	PRIMARY KEY (Email),
	FOREIGN KEY (Email) REFERENCES User(Email) ON UPDATE CASCADE,
	FOREIGN KEY (Email) REFERENCES User(Email) ON DELETE CASCADE
);

INSERT INTO MedicalRecord (PatientId, PatientName, Age, Height, Weight)
VALUES ("14237841", "Miguel Grilo", 21, 198, 87);