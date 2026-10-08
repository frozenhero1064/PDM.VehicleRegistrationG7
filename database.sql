CREATE DATABASE VehicleManagement;
USE VehicleManagement;

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

CREATE TABLE Owner (
    Owner_ID INT NOT NULL,
    Street VARCHAR(250),
    District VARCHAR(250),
    City VARCHAR(250),
    PRIMARY KEY (Owner_ID)
);

CREATE TABLE Individual (
    Owner_ID INT NOT NULL,
    Fname VARCHAR(50) NOT NULL,
    Mname VARCHAR(50) NOT NULL,
    Lname VARCHAR(50) NOT NULL,
    DoB DATE,
    Gender VARCHAR(10),
    Indentity_Card VARCHAR(50) UNIQUE,
    PRIMARY KEY (Owner_ID),
    FOREIGN KEY (Owner_ID) REFERENCES Owner(Owner_ID)
);

CREATE TABLE Organisation (
    Owner_ID INT NOT NULL,
    Company_Name VARCHAR(250) NOT NULL,
    Tax_Code VARCHAR(50) UNIQUE,
    Represent_Name VARCHAR(250),
    PRIMARY KEY (Owner_ID),
    FOREIGN KEY (Owner_ID) REFERENCES Owner(Owner_ID)
);

CREATE TABLE Owner_email (
    Owner_ID INT NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    PRIMARY KEY (Owner_ID),
    FOREIGN KEY (Owner_ID) REFERENCES Owner(Owner_ID)
);

CREATE TABLE Owner_phone (
    Owner_ID INT NOT NULL,
    Phone VARCHAR(20) NOT NULL,
    PRIMARY KEY (Owner_ID),
    FOREIGN KEY (Owner_ID) REFERENCES Owner(Owner_ID)
);

CREATE TABLE Ownership (
    Ownership_ID INT NOT NULL,
    Owner_ID INT NOT NULL,
    Start_Date DATE,
    End_Date DATE,
    Transfer_Type VARCHAR(100),
    PRIMARY KEY (Ownership_ID),
    FOREIGN KEY (Owner_ID) REFERENCES Owner(Owner_ID)
);

CREATE TABLE REGISTRATION (
    Registration_ID INT PRIMARY KEY,
    Registration_Date DATE,
    Expiry_Date DATE,
    Status VARCHAR(30),
    Issued_By VARCHAR(100),
    Plate_Num VARCHAR(20),
    FOREIGN KEY (Plate_Num) REFERENCES VEHICLE(Plate_Num)
);

CREATE TABLE INSURANCEPOLICY (
    Policy_Num VARCHAR(30) PRIMARY KEY,
    Policy_Name VARCHAR(100),
    Start_Date DATE,
    End_Date DATE,
    Insurance_Type VARCHAR(50),
    Plate_Num VARCHAR(20),
    FOREIGN KEY (Plate_Num) REFERENCES VEHICLE(Plate_Num)
);

CREATE TABLE INSPECTION (
    Inspection_ID INT PRIMARY KEY,
    Inspection_Date DATE,
    Result VARCHAR(30),
    Inspector_Name VARCHAR(100),
    Expiry_Date DATE,
    Notes VARCHAR(255),
    Plate_Num VARCHAR(20),
    FOREIGN KEY (Plate_Num) REFERENCES VEHICLE(Plate_Num)
);

CREATE TABLE TRAFFICVIOLATION (
    Violate_ID INT PRIMARY KEY,
    Violation_Type VARCHAR(100),
    Violation_Date DATE,
    Location VARCHAR(255),
    Description VARCHAR(255),
    Plate_Num VARCHAR(20),
    FOREIGN KEY (Plate_Num) REFERENCES VEHICLE(Plate_Num)
);

CREATE TABLE FINE (
    Fine_Num INT NOT NULL,
    Violate_ID INT NOT NULL,
    Amount DECIMAL(10,2),
    Due_Date DATE,
    Payment_Status VARCHAR(30),
    Paid_Date DATE,

    PRIMARY KEY (Fine_Num, Violate_ID),

    FOREIGN KEY (Violate_ID)
        REFERENCES TRAFFICVIOLATION(Violate_ID)
);
CREATE TABLE TRAFFICVIOLATION (
    Violate_ID INT PRIMARY KEY,
    Violation_Type VARCHAR(100),
    Violation_Date DATE,
    Location VARCHAR(255),
    Description VARCHAR(255),
    Plate_Num VARCHAR(20),

    FOREIGN KEY (Plate_Num)
        REFERENCES VEHICLE(Plate_Num)
);
--check table
SHOW TABLES;
