CREATE TABLE VEHICLE (
    Chassis_Num VARCHAR(50) PRIMARY KEY,
    Engine_Num VARCHAR(50) UNIQUE,
    Brand VARCHAR(50),
    Model VARCHAR(50),
    YOM INT,
    Color VARCHAR(30),
    Type VARCHAR(30),
    UNIQUE (Chassis_Num, Engine_Num)
);