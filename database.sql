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
create table Owner(
Owner_ID int NOT NULL,
Street varchar(250),
District varchar(250),
City varchar(250),
primary key(owner_ID)
);
create table Individual (
Owner_ID int NOT NULL,

Fname varchar(50) NOT NULL,
Mname varchar(50) NOT NULL,
Lname varchar(50) NOT NULL,

DoB date,

Gender varchar(10),
Indentity_Card varchar(50) UNIQUE,

primary key(Owner_ID),
foreign key(Owner_ID) references Owner(Owner_ID)
);
create table Organisation ( 
Owner_ID int NOT NULL,
Company_Name varchar(250) NOT NULL,
Tax_Code varchar(50) UNIQUE,
Represent_Name varchar(250),
primary key(Owner_ID),
foreign key(Owner_ID) references Owner(Owner_ID)
);
create table Owner_email(
Owner_ID int NOT NULL,
email varchar(100) not null unique,
primary key(Owner_ID),
foreign key(Owner_ID) references Owner(Owner_ID)
);
create table Owner_phone(
Owner_ID int NOT NULL,
Phone varchar(20) NOT NULL,
primary key(Owner_ID),
foreign key(Owner_ID) references Owner(Owner_ID)
);
create table Ownership(
Ownership_ID int NOT NULL,
Owner_ID int NOT NULL,
Start_Date date,
End_Date date,
Transfer_Type varchar (100),

primary key(Ownership_ID),
foreign key(Owner_ID) references Owner(Owner_ID)
);
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
CREATE TABLE FINE (
    Fine_Num INT PRIMARY KEY,
    Violate_ID INT,
    Amount DECIMAL(10,2),
    Due_Date DATE,
    Payment_Status VARCHAR(30),
    Paid_Date DATE,

    FOREIGN KEY (Violate_ID)
        REFERENCES TRAFFICVIOLATION(Violate_ID)
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
