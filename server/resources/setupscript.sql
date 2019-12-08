DROP TABLE IF EXISTS Medication;
DROP TABLE IF EXISTS MedicalRecord;
DROP TABLE IF EXISTS Auth;
DROP TABLE IF EXISTS User;

CREATE TABLE User (
	Id INT NOT NULL AUTO_INCREMENT,
	Email VARCHAR(500) UNIQUE,
	Name VARCHAR(500),
	Type VARCHAR(500),
	BirthDate VARCHAR(500),
	PRIMARY KEY (Id),
	CONSTRAINT chk_Type CHECK (Type IN ("Patient", "Doctor", "Staff", "Admin"))
);

CREATE TABLE MedicalRecord (
	Id INT NOT NULL AUTO_INCREMENT,
	PatientId INT,
	DoctorId INT,
	Weight VARCHAR(500),
	Height VARCHAR(500),
	PRIMARY KEY (Id)
);

ALTER TABLE MedicalRecord 
	ADD CONSTRAINT fk_patient_id 
		FOREIGN KEY (PatientId) 
		REFERENCES User(Id)
		ON DELETE CASCADE;
		
ALTER TABLE MedicalRecord 
	ADD CONSTRAINT fk_doctor_id 
		FOREIGN KEY (DoctorId) 
		REFERENCES User(Id);
		
CREATE TABLE Medication(
	Id INT NOT NULL AUTO_INCREMENT,
	MedicalRecordId INT,
	Name VARCHAR(500),
	PrescriptionDate VARCHAR(500),
	PRIMARY KEY (Id)
);

ALTER TABLE Medication 
	ADD CONSTRAINT fk_medical_record_id 
		FOREIGN KEY (MedicalRecordId) 
		REFERENCES MedicalRecord(Id)
		ON DELETE CASCADE;
		
CREATE TABLE Auth (
	Email VARCHAR(150),
	HashedPass VARCHAR(64),
	PRIMARY KEY (Email)
);

ALTER TABLE Auth 
	ADD CONSTRAINT fk_user_email 
		FOREIGN KEY (Email) 
		REFERENCES User(Email)
		ON DELETE CASCADE;

INSERT INTO User (Email, Name, Type, BirthDate)
VALUES ("miguel.grilo@gmail.com", "Miguel Grilo", "Patient", "19901010");

INSERT INTO MedicalRecord (PatientId, Weight, Height)
VALUES ("1", "80", "200");

INSERT INTO User (Email, Name, Type, BirthDate)
VALUES ("jorge.boce@gmail.com", "Jorge Jesus", "Patient", "19581113");

INSERT INTO MedicalRecord (PatientId, Weight, Height)
VALUES ("2", "77", "180");

INSERT INTO User (Email, Name, Type, BirthDate)
VALUES ("bob.maluco@gmail.com", "Bob Marley", "Doctor", "19500616");

INSERT INTO User (Email, Name, Type, BirthDate)
VALUES ("billie.jean@gmail.com", "Michael Jackson", "Doctor", "19670316");

INSERT INTO Medication (MedicalRecordId, Name, PrescriptionDate)
VALUES ("1", "Ben-u-ron", now());

INSERT INTO Medication (MedicalRecordId, Name, PrescriptionDate)
VALUES ("2", "Vicodin", now());