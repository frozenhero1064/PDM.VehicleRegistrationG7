CREATE TABLE TRAFFICVIOLATION (
    Violate_ID INT PRIMARY KEY,
    Violation_Type VARCHAR(100),
    Violation_Date DATE,
    Location VARCHAR(255),
    Description VARCHAR(255),
    Plate_Num VARCHAR(20),

    FOREIGN KEY (Plate_Num)
        REFERENCES VEHICLE(Plate_Num)
