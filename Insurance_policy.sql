CREATE TABLE INSURANCEPOLICY (
    Policy_Num VARCHAR(30) PRIMARY KEY,
    Policy_Name VARCHAR(100),
    Start_Date DATE,
    End_Date DATE,
    Insurance_Type VARCHAR(50),
    Plate_Num VARCHAR(20),

    FOREIGN KEY (Plate_Num)
        REFERENCES VEHICLE(Plate_Num)
);