CREATE TABLE VEHICLE (
    Plate_Num VARCHAR(20) PRIMARY KEY,
    Chassis_Num VARCHAR(50) UNIQUE,
    Engine_Num VARCHAR(50) UNIQUE,
    Brand VARCHAR(50),
    Model VARCHAR(50),
    YOM INT,
    Color VARCHAR(30),
    Type VARCHAR(30),
    Previous_Plate_Num VARCHAR(20) NULL,

    FOREIGN KEY (Previous_Plate_Num) 
        REFERENCES VEHICLE(Plate_Num)
);
