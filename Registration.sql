CREATE TABLE REGISTRATION (
	Plate_Num VARCHAR(20) PRIMARY KEY,
    Chassis_Num VARCHAR(50) UNIQUE,
    Engine_Num VARCHAR(50) UNIQUE,
    Registration_Date DATE,
    Expiry_Date DATE,
    Status VARCHAR(30),
    Issued_By VARCHAR(100),
    FOREIGN KEY (Chassis_Num, Engine_Num) REFERENCES VEHICLE(Chassis_Num, Engine_Num)
		ON DELETE CASCADE
        ON UPDATE CASCADE
);
