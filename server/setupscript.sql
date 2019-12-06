DROP TABLE IF EXISTS Auth;
DROP TABLE IF EXISTS User;
DROP TABLE IF EXISTS MedicalRecord;


CREATE TABLE User (
	UserId INT,
	Email VARCHAR(150) UNIQUE,
	Name VARCHAR(150),
	Type VARCHAR(100),
	PRIMARY KEY (UserId),
	CONSTRAINT chk_Type CHECK (Type IN ("Patient", "Doctor", "Staff", "Admin"))
);

CREATE TABLE MedicalRecord (
	RecordId INT NOT NULL AUTO_INCREMENT,
	UserId INT,
	PatientName VARCHAR(150), 
	Age INT,
	Weight INT,
	Height INT,
	PRIMARY KEY (RecordId),
	FOREIGN KEY (UserId) REFERENCES User(UserId) ON UPDATE CASCADE,
	FOREIGN KEY (UserId) REFERENCES User(UserId) ON DELETE CASCADE
);


CREATE TABLE Auth (
	Email VARCHAR(150),
	HashedPass VARCHAR(64),
	PRIMARY KEY (Email),
	FOREIGN KEY (Email) REFERENCES User(Email) ON UPDATE CASCADE,
	FOREIGN KEY (Email) REFERENCES User(Email) ON DELETE CASCADE
);

INSERT INTO User (UserId, Email, Name, Type)
VALUES ("14237841", "miguel.grilo@gmail.com", "Miguel Grilo", "Patient");

INSERT INTO MedicalRecord (UserId, PatientName, Age, Height, Weight)
VALUES ("14237841", "Miguel Grilo", 21, 198, 87);