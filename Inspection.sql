CREATE TABLE INSPECTION (
    Inspection_ID INT PRIMARY KEY,
    Inspection_Date DATE,
    Result VARCHAR(30),
    Inspector_Name VARCHAR(100),
    Expiry_Date DATE,
    Notes VARCHAR(255),
    Plate_Num VARCHAR(20),

    FOREIGN KEY (Plate_Num)
        REFERENCES VEHICLE(Plate_Num)
);