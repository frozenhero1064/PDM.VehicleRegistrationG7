CREATE TABLE REGISTRATION (
    Registration_ID INT PRIMARY KEY,
    Registration_Date DATE,
    Expiry_Date DATE,
    Status VARCHAR(30),
    Issued_By VARCHAR(100),
    Plate_Num VARCHAR(20),

    FOREIGN KEY (Plate_Num)
        REFERENCES VEHICLE(Plate_Num)
);