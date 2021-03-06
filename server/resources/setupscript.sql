DROP TABLE IF EXISTS Medication;
DROP TABLE IF EXISTS Treatment;
DROP TABLE IF EXISTS Diagnosis;
DROP TABLE IF EXISTS MedicalRecord;
DROP TABLE IF EXISTS Auth;
DROP TABLE IF EXISTS User;

CREATE TABLE User (
	Id INT NOT NULL AUTO_INCREMENT,
	Name VARCHAR(500),
	Type VARCHAR(500),
	BirthDate VARCHAR(500),
	PRIMARY KEY (Id)
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
	ADD CONSTRAINT fk_medical_record_med_id 
		FOREIGN KEY (MedicalRecordId) 
		REFERENCES MedicalRecord(Id)
		ON DELETE CASCADE;

CREATE TABLE Diagnosis(
	Id INT NOT NULL AUTO_INCREMENT,
	MedicalRecordId INT,
	Name VARCHAR(500),
	Description VARCHAR(500),
	DiagnosisDate VARCHAR(500),
	PRIMARY KEY (Id)
);

ALTER TABLE Diagnosis 
	ADD CONSTRAINT fk_medical_record_dia_id 
		FOREIGN KEY (MedicalRecordId) 
		REFERENCES MedicalRecord(Id)
		ON DELETE CASCADE;
		
CREATE TABLE Treatment(
	Id INT NOT NULL AUTO_INCREMENT,
	MedicalRecordId INT,
	Name VARCHAR(500),
	Description VARCHAR(500),
	TreatmentDate VARCHAR(500),
	PRIMARY KEY (Id)
);

ALTER TABLE Treatment 
	ADD CONSTRAINT fk_medical_record_tre_id 
		FOREIGN KEY (MedicalRecordId) 
		REFERENCES MedicalRecord(Id)
		ON DELETE CASCADE;
		
CREATE TABLE Auth (
	UserId INT,
	HashedPass VARCHAR(64),
	PRIMARY KEY (UserId)
);

ALTER TABLE Auth 
	ADD CONSTRAINT fk_user_id
		FOREIGN KEY (UserId) 
		REFERENCES User(Id)
		ON DELETE CASCADE;
