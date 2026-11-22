DROP DATABASE IF EXISTS vehiclemanagement;
CREATE DATABASE IF NOT EXISTS VehicleManagement;
USE VehicleManagement;

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

CREATE TABLE Owner (
    OwnerID INT NOT NULL AUTO_INCREMENT,
    Street VARCHAR(250),
    District VARCHAR(250),
    City VARCHAR(250),
    PRIMARY KEY (OwnerID)
);

CREATE TABLE Individual (
    OwnerID INT NOT NULL,
    Fname VARCHAR(50) NOT NULL,
    Mname VARCHAR(50) NOT NULL,
    Lname VARCHAR(50) NOT NULL,
    DoB DATE,
    Gender VARCHAR(10),
    Identity_Card VARCHAR(50) UNIQUE,
    PRIMARY KEY (OwnerID),
    FOREIGN KEY (OwnerID) REFERENCES Owner(OwnerID)
		ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE Organisation (
    OwnerID INT NOT NULL,
    Company_Name VARCHAR(250) NOT NULL,
    Tax_Code VARCHAR(50) UNIQUE,
    Represent_Name VARCHAR(250),
    PRIMARY KEY (OwnerID),
    FOREIGN KEY (OwnerID) REFERENCES Owner(OwnerID)
		ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE Owner_email (
    OwnerID INT NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    PRIMARY KEY (OwnerID, email),
    FOREIGN KEY (OwnerID) REFERENCES Owner(OwnerID)
		ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE Owner_phone (
    OwnerID INT NOT NULL,
    Phone VARCHAR(20) NOT NULL,
    PRIMARY KEY (OwnerID, phone),
    FOREIGN KEY (OwnerID) REFERENCES Owner(OwnerID)
		ON DELETE CASCADE
        ON UPDATE CASCADE
);

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

CREATE TABLE Ownership (
    Ownership_ID INT NOT NULL AUTO_INCREMENT,
    OwnerID INT NOT NULL,
    Plate_Num VARCHAR(20),
    Start_Date DATE,
    End_Date DATE,
    Transfer_Type VARCHAR(100),
    PRIMARY KEY (Ownership_ID),
    FOREIGN KEY (OwnerID) REFERENCES Owner(OwnerID)
    	ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (Plate_Num) REFERENCES REGISTRATION(Plate_Num)
		ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE INSURANCEPOLICY (
    Policy_Num INT PRIMARY KEY AUTO_INCREMENT,
    Policy_Name VARCHAR(100),
    Start_Date DATE,
    End_Date DATE,
    Insurance_Type VARCHAR(50),
    Plate_Num VARCHAR(20),
    FOREIGN KEY (Plate_Num) REFERENCES REGISTRATION(Plate_Num)
    	ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE INSPECTION (
    Inspection_ID INT PRIMARY KEY AUTO_INCREMENT,
    Inspection_Date DATE,
    Result VARCHAR(30),
    Inspector_Name VARCHAR(100),
    Expiry_Date DATE,
    Notes VARCHAR(255),
    Plate_Num VARCHAR(20),
    FOREIGN KEY (Plate_Num) REFERENCES REGISTRATION(Plate_Num)
		ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE TRAFFICVIOLATION (
    Violate_ID INT PRIMARY KEY AUTO_INCREMENT,
    Violation_Type VARCHAR(100),
    Violation_Date DATE,
    Location VARCHAR(255),
    Description VARCHAR(255),
    Plate_Num VARCHAR(20),
    FOREIGN KEY (Plate_Num) REFERENCES REGISTRATION(Plate_Num)
    	ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE FINE (
    Violate_ID INT NOT NULL,
    Amount DECIMAL(10,2),
    Due_Date DATE,
    Payment_Status VARCHAR(30),
    Paid_Date DATE,
    PRIMARY KEY (Violate_ID),
    FOREIGN KEY (Violate_ID) REFERENCES TRAFFICVIOLATION(Violate_ID)
		ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- 1. VEHICLE
INSERT INTO VEHICLE (Chassis_Num, Engine_Num, Brand, Model, YOM, Color, Type) VALUES
('CHS001ENG01', 'ENG100001', 'Toyota', 'Vios', 2018, 'White', 'Sedan'),
('CHS002ENG02', 'ENG100002', 'Honda', 'Civic', 2020, 'Black', 'Sedan'),
('CHS003ENG03', 'ENG100003', 'Mazda', 'CX-5', 2021, 'Red', 'SUV'),
('CHS004ENG04', 'ENG100004', 'Ford', 'Ranger', 2019, 'Blue', 'Pickup'),
('CHS005ENG05', 'ENG100005', 'Hyundai', 'Accent', 2022, 'Silver', 'Sedan'),
('CHS006ENG06', 'ENG100006', 'VinFast', 'VF8', 2023, 'Green', 'SUV'),
('CHS007ENG07', 'ENG100007', 'Toyota', 'Fortuner', 2017, 'Black', 'SUV'),
('CHS008ENG08', 'ENG100008', 'Kia', 'Morning', 2018, 'Yellow', 'Hatchback'),
('CHS009ENG09', 'ENG100009', 'Mercedes', 'C200', 2021, 'White', 'Sedan'),
('CHS010ENG10', 'ENG100010', 'BMW', 'X5', 2022, 'Black', 'SUV'),
('CHS011ENG11', 'ENG100011', 'Honda', 'City', 2019, 'Gray', 'Sedan'),
('CHS012ENG12', 'ENG100012', 'Hyundai', 'Santa Fe', 2020, 'White', 'SUV'),
('CHS013ENG13', 'ENG100013', 'VinFast', 'VF e34', 2022, 'Blue', 'SUV'),
('CHS014ENG14', 'ENG100014', 'Toyota', 'Camry', 2021, 'Black', 'Sedan'),
('CHS015ENG15', 'ENG100015', 'Ford', 'Transit', 2018, 'White', 'Van');

-- 2. OWNER
INSERT INTO Owner (OwnerID, Street, District, City) VALUES
(1, '12 Le Duan', 'District 1', 'Ho Chi Minh City'),
(2, '45 Ba Trieu', 'Hoan Kiem', 'Hanoi'),
(3, '78 Nguyen Van Linh', 'Hai Chau', 'Da Nang'),
(4, '102 Tran Phu', 'Ninh Kieu', 'Can Tho'),
(5, '55 Le Hong Phong', 'Ngo Quyen', 'Hai Phong'),
(6, '89 Pham Van Dong', 'Cau Giay', 'Hanoi'),
(7, '120 Vo Van Tan', 'District 3', 'Ho Chi Minh City'),
(8, '14 Nguyen Hue', 'District 1', 'Ho Chi Minh City'),
(9, '22 Hung Vuong', 'Thanh Khe', 'Da Nang'),
(10, '33 Cach Mang Thang Tam', 'District 10', 'Ho Chi Minh City'),
(11, '77 Hoang Dieu', 'Ba Dinh', 'Hanoi'),
(12, '99 Phan Chu Trinh', 'Hai Chau', 'Da Nang'),
(13, '40 Tran Hung Dao', 'Hoan Kiem', 'Hanoi'),
(14, '15 Le Lai', 'District 1', 'Ho Chi Minh City'),
(15, '66 Nguyen Trai', 'Thanh Xuan', 'Hanoi');

-- 3. INDIVIDUAL
INSERT INTO Individual (OwnerID, Fname, Mname, Lname, DoB, Gender, Identity_Card) VALUES
(1, 'Nguyen', 'Van', 'An', '1985-05-12', 'Male', '079085001234'),
(2, 'Tran', 'Thi', 'Binh', '1990-08-22', 'Female', '001090005678'),
(3, 'Le', 'Hoang', 'Cuong', '1982-12-03', 'Male', '048082009876'),
(4, 'Pham', 'Minh', 'Dung', '1995-03-15', 'Male', '092095004321'),
(5, 'Hoang', 'Thu', 'Ha', '1988-11-20', 'Female', '031088007890'),
(6, 'Vu', 'Quang', 'Huy', '1992-07-07', 'Male', '001092001122'),
(7, 'Dang', 'Thi', 'Mai', '1998-01-25', 'Female', '079098003344'),
(8, 'Bui', 'Van', 'Nam', '1980-09-10', 'Male', '079080005566'),
(9, 'Do', 'Thi', 'Oanh', '1991-04-18', 'Female', '048091007788'),
(10, 'Ngo', 'Duc', 'Phuc', '1987-06-30', 'Male', '079087009900');

-- 4. ORGANISATION
INSERT INTO Organisation (OwnerID, Company_Name, Tax_Code, Represent_Name) VALUES
(11, 'FPT Software Co., Ltd.', '0101781820', 'Nguyen Van A'),
(12, 'Viettel Group', '0100109106', 'Le Dang D.'),
(13, 'Vingroup JSC', '0101245486', 'Pham Nhat V.'),
(14, 'Vinamilk JSC', '0300588569', 'Mai Kieu L.'),
(15, 'Thaco Truong Hai', '3700201416', 'Tran Ba D.');

-- 5. OWNER_EMAIL
INSERT INTO Owner_email (OwnerID, email) VALUES
(1, 'an.nguyen@gmail.com'),
(2, 'binh.tran@yahoo.com'),
(3, 'cuong.le@outlook.com'),
(4, 'dung.pham@gmail.com'),
(5, 'ha.hoang@gmail.com'),
(6, 'huy.vu@fpt.com'),
(7, 'mai.dang@gmail.com'),
(8, 'nam.bui@viettel.com.vn'),
(9, 'oanh.do@vingroup.net'),
(10, 'phuc.ngo@vinamilk.com.vn'),
(11, 'contact@fpt.com'),
(12, 'support@viettel.com.vn'),
(13, 'info@vingroup.net'),
(14, 'contact@vinamilk.com.vn'),
(15, 'info@thaco.com.vn');

-- 6. OWNER_PHONE
INSERT INTO Owner_phone (OwnerID, Phone) VALUES
(1, '0901112233'),
(2, '0912223344'),
(3, '0923334455'),
(4, '0934445566'),
(5, '0945556677'),
(6, '0956667788'),
(7, '0967778899'),
(8, '0978889900'),
(9, '0989990011'),
(10, '0990001122'),
(11, '02437680000'),
(12, '02462556789'),
(13, '02439749999'),
(14, '02854155555'),
(15, '02538383838');

-- 7. REGISTRATION
INSERT INTO REGISTRATION (Plate_Num, Chassis_Num, Engine_Num, Registration_Date, Expiry_Date, Issued_By) VALUES
('29A-12345', 'CHS001ENG01', 'ENG100001', '2018-03-05', '2028-03-05', 'Cuc CSGT HN'),
('30F-54321', 'CHS002ENG02', 'ENG100002', '2020-05-20', '2030-05-20', 'Cuc CSGT HN'),
('51G-99887', 'CHS003ENG03', 'ENG100003', '2021-01-15', '2031-01-15', 'Cuc CSGT DN'),
('43C-11223', 'CHS004ENG04', 'ENG100004', '2019-07-25', '2029-07-25', 'Cuc CSGT CT'),
('60B-33445', 'CHS005ENG05', 'ENG100005', '2022-02-15', '2032-02-15', 'Cuc CSGT HP'),
('51H-66778', 'CHS006ENG06', 'ENG100006', '2023-04-10', '2033-04-10', 'Cuc CSGT HN'),
('29C-44556', 'CHS007ENG07', 'ENG100007', '2017-08-25', '2027-08-25', 'Cuc CSGT HN'),
('30G-77889', 'CHS008ENG08', 'ENG100008', '2018-10-15', '2028-10-15', 'Cuc CSGT HN'),
('51F-13579', 'CHS009ENG09', 'ENG100009', '2021-07-01', '2031-07-01', 'Cuc CSGT SG'),
('61D-24680', 'CHS010ENG10', 'ENG100010', '2022-09-20', '2032-09-20', 'Cuc CSGT BD'),
('15A-98765', 'CHS011ENG11', 'ENG100011', '2019-11-05', '2029-11-05', 'Cuc CSGT HN'),
('37B-55443', 'CHS012ENG12', 'ENG100012', '2020-12-15', '2025-12-15', 'Cuc CSGT HN'),
('51K-11122', 'CHS013ENG13', 'ENG100013', '2022-03-30', '2032-03-30', 'Cuc CSGT SG'),
('29D-33221', 'CHS014ENG14', 'ENG100014', '2023-01-15', '2033-01-15', 'Cuc CSGT HN'),
('30E-99001', 'CHS015ENG15', 'ENG100015', '2018-06-20', '2028-06-20', 'Cuc CSGT HN');

-- 8. OWNERSHIP
INSERT INTO Ownership (Ownership_ID, OwnerID, Plate_Num, Start_Date, End_Date, Transfer_Type) VALUES
(1,  1,  '51H-66778', '2018-03-01', NULL, 'New Purchase'),
(2,  2,  '29D-33221', '2020-05-15', NULL, 'New Purchase'),
(3,  3,  '30E-99001', '2021-01-10', NULL, 'New Purchase'),
(4,  4,  '51F-13579', '2019-07-20', NULL, 'New Purchase'),
(5,  5,  '29A-12345', '2022-02-11', NULL, 'New Purchase'),
(6,  6,  '61D-24680', '2023-04-05', NULL, 'New Purchase'),
(7,  7,  '43C-11223', '2017-08-19', NULL, 'New Purchase'),
(8,  8,  '51K-11122', '2018-10-10', NULL, 'New Purchase'),
(9,  9,  '30F-54321', '2021-06-30', NULL, 'New Purchase'),
(10, 10, '37B-55443', '2022-09-14', NULL, 'New Purchase'),
(11, 11, '60B-33445', '2019-11-01', NULL, 'New Purchase'),
(12, 12, '15A-98765', '2020-12-12', NULL, 'New Purchase'),
(13, 13, '29C-44556', '2022-03-25', NULL, 'New Purchase'),
(14, 14, '51G-99887', '2021-08-08', NULL, 'New Purchase'),
(15, 15, '30G-77889', '2018-06-15', NULL, 'New Purchase');

-- 9. INSURANCEPOLICY
INSERT INTO INSURANCEPOLICY (Policy_Name, Start_Date, End_Date, Insurance_Type, Plate_Num) VALUES
('Bao hiem TNDS 1', '2026-01-01', '2027-01-01', 'Compulsory', '29A-12345'),
('Bao hiem vat chat 2', '2026-02-01', '2027-02-01', 'Comprehensive', '30F-54321'),
('Bao hiem TNDS 3', '2026-03-01', '2027-03-01', 'Compulsory', '51G-99887'),
('Bao hiem xe tai 4', '2026-04-01', '2027-04-01', 'Compulsory', '43C-11223'),
('Bao hiem toan dien 5', '2026-05-01', '2027-05-01', 'Comprehensive', '60B-33445'),
('Bao hiem xe dien 6', '2026-06-01', '2027-06-01', 'Comprehensive', '51H-66778'),
('Bao hiem SUV 7', '2026-01-15', '2027-01-15', 'Compulsory', '29C-44556'),
('Bao hiem mini 8', '2026-02-15', '2027-02-15', 'Compulsory', '30G-77889'),
('Bao hiem Merc 9', '2026-03-15', '2027-03-15', 'Comprehensive', '51F-13579'),
('Bao hiem BMW 10', '2026-04-15', '2027-04-15', 'Comprehensive', '61D-24680'),
('Bao hiem Honda 11', '2026-05-15', '2027-05-15', 'Compulsory', '15A-98765'),
('Bao hiem SantaFe 12', '2026-06-15', '2027-06-15', 'Comprehensive', '37B-55443'),
('Bao hiem VinFast 13', '2026-01-20', '2027-01-20', 'Compulsory', '51K-11122'),
('Bao hiem Camry 14', '2026-02-20', '2027-02-20', 'Comprehensive', '29D-33221'),
('Bao hiem Transit 15', '2026-03-20', '2027-03-20', 'Compulsory', '30E-99001');

-- 10. INSPECTION
INSERT INTO INSPECTION (Inspection_Date, Result, Inspector_Name, Expiry_Date, Notes, Plate_Num) VALUES
('2025-03-01', 'Pass', 'Nguyen Van Thanh', '2026-03-01', 'Standard check', '29A-12345'),
('2025-04-01', 'Pass', 'Tran Duc Bo', '2026-04-01', 'Good condition', '30F-54321'),
('2025-05-01', 'Pass', 'Le Minh Hoang', '2026-05-01', 'Standard check', '51G-99887'),
('2025-06-01', 'Pass', 'Pham Tuan', '2026-06-01', 'Good condition', '43C-11223'),
('2025-07-01', 'Pass', 'Hoang Hai', '2026-07-01', 'Standard check', '60B-33445'),
('2025-08-01', 'Pass', 'Vu Ngoc', '2026-08-01', 'EV check passed', '51H-66778'),
('2025-01-10', 'Pass', 'Dang Khoa', '2026-01-10', 'Standard check', '29C-44556'),
('2025-02-10', 'Pass', 'Bui Viet', '2026-02-10', 'Standard check', '30G-77889'),
('2025-03-10', 'Pass', 'Do Manh', '2026-03-10', 'Luxury check', '51F-13579'),
('2025-04-10', 'Pass', 'Ngo Thang', '2026-04-10', 'SUV check', '61D-24680'),
('2025-05-10', 'Pass', 'Phan Son', '2026-05-10', 'Standard check', '15A-98765'),
('2025-06-10', 'Pass', 'Trinh Long', '2026-06-10', 'Standard check', '37B-55443'),
('2025-07-10', 'Pass', 'Duong Tung', '2026-07-10', 'EV check', '51K-11122'),
('2025-08-10', 'Pass', 'Mai Linh', '2026-08-10', 'Standard check', '29D-33221'),
('2025-09-10', 'Pass', 'Dao Anh', '2026-09-10', 'Commercial check', '30E-99001');

-- 11. TRAFFICVIOLATION
INSERT INTO TRAFFICVIOLATION (Violation_Type, Violation_Date, Location, Description, Plate_Num) VALUES
('Speeding', '2026-01-10', 'Phap Van - Cau Gie', 'Exceeded speed limit by 15km/h', '29A-12345'),
('Red Light', '2026-01-12', 'Ngã tư Láng Hạ', 'Ran a red traffic light', '30F-54321'),
('Wrong Lane', '2026-01-15', 'Vo Van Kiet', 'Drove in restricted lane', '51G-99887'),
('Illegal Parking', '2026-01-18', 'Nguyen Hue, D1', 'Parked in a no-parking zone', '43C-11223'),
('Speeding', '2026-01-20', 'Cao toc Long Thanh', 'Exceeded speed limit by 25km/h', '60B-33445'),
('No Helmet / Seatbelt', '2026-01-22', 'Cau Giay', 'Driver not wearing seatbelt', '51H-66778'),
('Wrong Lane', '2026-01-25', 'Thang Long Blvd', 'Improper lane changing', '29C-44556'),
('Red Light', '2026-01-28', 'Hai Ba Trung', 'Ran a red traffic light', '30G-77889'),
('Speeding', '2026-02-01', 'Sai Gon Bridge', 'Exceeded speed limit', '51F-13579'),
('Illegal Parking', '2026-02-03', 'Dong Khoi', 'Double parking', '61D-24680'),
('Speeding', '2026-02-05', 'Nhat Tan Bridge', 'Exceeded speed limit', '15A-98765'),
('Red Light', '2026-02-08', 'Giang Vo', 'Ran a red light', '37B-55443'),
('Wrong Lane', '2026-02-10', 'Nguyen Van Linh', 'Improper turning', '51K-11122'),
('Illegal Parking', '2026-02-12', 'Ba Trieu', 'Parked on sidewalk', '29D-33221'),
('Speeding', '2026-02-15', 'Trung Hoa', 'Exceeded speed limit', '30E-99001');

-- 12. FINE
INSERT INTO FINE (Violate_ID, Amount, Due_Date, Payment_Status, Paid_Date) VALUES
(1, 1000000.00, '2026-02-10', 'Paid', '2026-01-15'),
(2, 2000000.00, '2026-02-12', 'Paid', '2026-01-18'),
(3, 1500000.00, '2026-02-15', 'Unpaid', NULL),
(4, 800000.00, '2026-02-18', 'Paid', '2026-01-20'),
(5, 4000000.00, '2026-02-20', 'Unpaid', NULL),
(6, 500000.00, '2026-02-22', 'Paid', '2026-01-25'),
(7, 1500000.00, '2026-02-25', 'Unpaid', NULL),
(8, 2000000.00, '2026-02-28', 'Paid', '2026-02-01'),
(9, 3000000.00, '2026-03-01', 'Unpaid', NULL),
(10, 800000.00, '2026-03-03', 'Paid', '2026-02-05'),
(11, 1000000.00, '2026-03-05', 'Paid', '2026-02-07'),
(12, 2000000.00, '2026-03-08', 'Unpaid', NULL),
(13, 1500000.00, '2026-03-10', 'Paid', '2026-02-11'),
(14, 800000.00, '2026-03-12', 'Unpaid', NULL),
(15, 2500000.00, '2026-03-15', 'Paid', '2026-02-18');

USE VehicleManagement;
DROP PROCEDURE IF EXISTS sp_PayFine;
DELIMITER //

CREATE PROCEDURE sp_PayFine(
    IN p_ViolateID INT
)
BEGIN
    DECLARE v_Status VARCHAR(30);

    DECLARE EXIT HANDLER FOR SQLSTATE '45000'
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Transaction failed: Could not process fine payment.';
    END;

    START TRANSACTION;

        -- Check current payment status
        SELECT Payment_Status INTO v_Status
        FROM FINE
        WHERE Violate_ID = p_ViolateID
        FOR UPDATE;

        IF v_Status = 'Paid' THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Fine is already paid.';
        ELSE
            UPDATE FINE
            SET Payment_Status = 'Paid', Paid_Date = CURDATE()
            WHERE Violate_ID = p_ViolateID;
        END IF;

    COMMIT;
END //

DELIMITER ;

USE VehicleManagement;
DROP PROCEDURE IF EXISTS sp_TransferVehicleOwnership;
DELIMITER //

CREATE PROCEDURE sp_TransferVehicleOwnership(
	IN p_OldPlate VARCHAR(20),
    IN p_NewPlate VARCHAR(20),
    IN p_NewOwnerID INT,
    IN p_NewOwnershipID INT,
    IN p_NewIssuedBy VARCHAR(100)
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
        -- SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Transaction failed: Ownership transfer aborted.';
    END;

    START TRANSACTION;

		-- Set the old ownership to not owned anymore
        UPDATE Ownership
        SET End_Date = CURDATE(), Transfer_Type = "Transfered"
        WHERE Plate_Num = p_OldPlate AND Ownership_ID > 0;

		-- Update old registration
		UPDATE REGISTRATION
		SET Plate_Num = p_NewPlate,
			Registration_Date = CURDATE(),
			Expiry_Date = DATE_ADD(CURDATE(), INTERVAL 10 YEAR),
			Status = 'Active',
			Issued_By = p_NewIssuedBy
		WHERE Plate_Num = p_OldPlate;

        -- Create new ownership link
        INSERT INTO Ownership (Ownership_ID, OwnerID, Plate_Num, Start_Date, End_Date, Transfer_Type)
        VALUES (p_NewOwnershipID, p_NewOwnerID, p_NewPlate, CURDATE(), NULL, 'Transfer In');

    COMMIT;
END //

DELIMITER ;