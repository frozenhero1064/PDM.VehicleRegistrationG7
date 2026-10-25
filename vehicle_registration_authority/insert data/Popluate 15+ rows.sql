USE VehicleManagement;

-- 1. VEHICLE
INSERT INTO VEHICLE (Plate_Num, Chassis_Num, Engine_Num, Brand, Model, YOM, Color, Type, Previous_Plate_Num) VALUES
('29A-12345', 'CHS001ENG01', 'ENG100001', 'Toyota', 'Vios', 2018, 'White', 'Sedan', NULL),
('30F-54321', 'CHS002ENG02', 'ENG100002', 'Honda', 'Civic', 2020, 'Black', 'Sedan', NULL),
('51G-99887', 'CHS003ENG03', 'ENG100003', 'Mazda', 'CX-5', 2021, 'Red', 'SUV', NULL),
('43C-11223', 'CHS004ENG04', 'ENG100004', 'Ford', 'Ranger', 2019, 'Blue', 'Pickup', NULL),
('60B-33445', 'CHS005ENG05', 'ENG100005', 'Hyundai', 'Accent', 2022, 'Silver', 'Sedan', NULL),
('51H-66778', 'CHS006ENG06', 'ENG100006', 'VinFast', 'VF8', 2023, 'Green', 'SUV', NULL),
('29C-44556', 'CHS007ENG07', 'ENG100007', 'Toyota', 'Fortuner', 2017, 'Black', 'SUV', NULL),
('30G-77889', 'CHS008ENG08', 'ENG100008', 'Kia', 'Morning', 2018, 'Yellow', 'Hatchback', NULL),
('51F-13579', 'CHS009ENG09', 'ENG100009', 'Mercedes', 'C200', 2021, 'White', 'Sedan', NULL),
('61D-24680', 'CHS010ENG10', 'ENG100010', 'BMW', 'X5', 2022, 'Black', 'SUV', NULL),
('15A-98765', 'CHS011ENG11', 'ENG100011', 'Honda', 'City', 2019, 'Gray', 'Sedan', NULL),
('37B-55443', 'CHS012ENG12', 'ENG100012', 'Hyundai', 'Santa Fe', 2020, 'White', 'SUV', NULL),
('51K-11122', 'CHS013ENG13', 'ENG100013', 'VinFast', 'VF e34', 2022, 'Blue', 'SUV', NULL),
('29D-33221', 'CHS014ENG14', 'ENG100014', 'Toyota', 'Camry', 2021, 'Black', 'Sedan', '29A-12345'),
('30E-99001', 'CHS015ENG15', 'ENG100015', 'Ford', 'Transit', 2018, 'White', 'Van', NULL);

-- 2. OWNER
INSERT INTO Owner (Owner_ID, Street, District, City) VALUES
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
INSERT INTO Individual (Owner_ID, Fname, Mname, Lname, DoB, Gender, Indentity_Card) VALUES
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
INSERT INTO Organisation (Owner_ID, Company_Name, Tax_Code, Represent_Name) VALUES
(11, 'FPT Software Co., Ltd.', '0101781820', 'Nguyen Van A'),
(12, 'Viettel Group', '0100109106', 'Le Dang D.'),
(13, 'Vingroup JSC', '0101245486', 'Pham Nhat V.'),
(14, 'Vinamilk JSC', '0300588569', 'Mai Kieu L.'),
(15, 'Thaco Truong Hai', '3700201416', 'Tran Ba D.');

-- 5. OWNER_EMAIL
INSERT INTO Owner_email (Owner_ID, email) VALUES
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
INSERT INTO Owner_phone (Owner_ID, Phone) VALUES
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

-- 7. OWNERSHIP
INSERT INTO Ownership (Ownership_ID, Owner_ID, Start_Date, End_Date, Transfer_Type) VALUES
(1, 1, '2018-03-01', NULL, 'New Purchase'),
(2, 2, '2020-05-15', NULL, 'New Purchase'),
(3, 3, '2021-01-10', NULL, 'New Purchase'),
(4, 4, '2019-07-20', NULL, 'New Purchase'),
(5, 5, '2022-02-11', NULL, 'New Purchase'),
(6, 6, '2023-04-05', NULL, 'New Purchase'),
(7, 7, '2017-08-19', NULL, 'New Purchase'),
(8, 8, '2018-10-10', NULL, 'New Purchase'),
(9, 9, '2021-06-30', NULL, 'New Purchase'),
(10, 10, '2022-09-14', NULL, 'New Purchase'),
(11, 11, '2019-11-01', NULL, 'New Purchase'),
(12, 12, '2020-12-12', NULL, 'New Purchase'),
(13, 13, '2022-03-25', NULL, 'New Purchase'),
(14, 14, '2021-08-08', '2023-01-10', 'Transfer'),
(15, 15, '2018-06-15', NULL, 'New Purchase');

-- 8. REGISTRATION
INSERT INTO REGISTRATION (Registration_ID, Registration_Date, Expiry_Date, Status, Issued_By, Plate_Num) VALUES
(1, '2018-03-05', '2028-03-05', 'Active', 'Cuc CSGT HN', '29A-12345'),
(2, '2020-05-20', '2030-05-20', 'Active', 'Cuc CSGT HN', '30F-54321'),
(3, '2021-01-15', '2031-01-15', 'Active', 'Cuc CSGT DN', '51G-99887'),
(4, '2019-07-25', '2029-07-25', 'Active', 'Cuc CSGT CT', '43C-11223'),
(5, '2022-02-15', '2032-02-15', 'Active', 'Cuc CSGT HP', '60B-33445'),
(6, '2023-04-10', '2033-04-10', 'Active', 'Cuc CSGT HN', '51H-66778'),
(7, '2017-08-25', '2027-08-25', 'Active', 'Cuc CSGT HN', '29C-44556'),
(8, '2018-10-15', '2028-10-15', 'Active', 'Cuc CSGT HN', '30G-77889'),
(9, '2021-07-01', '2031-07-01', 'Active', 'Cuc CSGT SG', '51F-13579'),
(10, '2022-09-20', '2032-09-20', 'Active', 'Cuc CSGT BD', '61D-24680'),
(11, '2019-11-05', '2029-11-05', 'Active', 'Cuc CSGT HN', '15A-98765'),
(12, '2020-12-15', '2030-12-15', 'Active', 'Cuc CSGT HN', '37B-55443'),
(13, '2022-03-30', '2032-03-30', 'Active', 'Cuc CSGT SG', '51K-11122'),
(14, '2023-01-15', '2033-01-15', 'Active', 'Cuc CSGT HN', '29D-33221'),
(15, '2018-06-20', '2028-06-20', 'Active', 'Cuc CSGT HN', '30E-99001');

-- 9. INSURANCEPOLICY
INSERT INTO INSURANCEPOLICY (Policy_Num, Policy_Name, Start_Date, End_Date, Insurance_Type, Plate_Num) VALUES
('POL001', 'Bao hiem TNDS 1', '2026-01-01', '2027-01-01', 'Compulsory', '29A-12345'),
('POL002', 'Bao hiem vat chat 2', '2026-02-01', '2027-02-01', 'Comprehensive', '30F-54321'),
('POL003', 'Bao hiem TNDS 3', '2026-03-01', '2027-03-01', 'Compulsory', '51G-99887'),
('POL004', 'Bao hiem xe tai 4', '2026-04-01', '2027-04-01', 'Compulsory', '43C-11223'),
('POL005', 'Bao hiem toan dien 5', '2026-05-01', '2027-05-01', 'Comprehensive', '60B-33445'),
('POL006', 'Bao hiem xe dien 6', '2026-06-01', '2027-06-01', 'Comprehensive', '51H-66778'),
('POL007', 'Bao hiem SUV 7', '2026-01-15', '2027-01-15', 'Compulsory', '29C-44556'),
('POL008', 'Bao hiem mini 8', '2026-02-15', '2027-02-15', 'Compulsory', '30G-77889'),
('POL009', 'Bao hiem Merc 9', '2026-03-15', '2027-03-15', 'Comprehensive', '51F-13579'),
('POL010', 'Bao hiem BMW 10', '2026-04-15', '2027-04-15', 'Comprehensive', '61D-24680'),
('POL011', 'Bao hiem Honda 11', '2026-05-15', '2027-05-15', 'Compulsory', '15A-98765'),
('POL012', 'Bao hiem SantaFe 12', '2026-06-15', '2027-06-15', 'Comprehensive', '37B-55443'),
('POL013', 'Bao hiem VinFast 13', '2026-01-20', '2027-01-20', 'Compulsory', '51K-11122'),
('POL014', 'Bao hiem Camry 14', '2026-02-20', '2027-02-20', 'Comprehensive', '29D-33221'),
('POL015', 'Bao hiem Transit 15', '2026-03-20', '2027-03-20', 'Compulsory', '30E-99001');

-- 10. INSPECTION
INSERT INTO INSPECTION (Inspection_ID, Inspection_Date, Result, Inspector_Name, Expiry_Date, Notes, Plate_Num) VALUES
(1, '2025-03-01', 'Pass', 'Nguyen Van Thanh', '2026-03-01', 'Standard check', '29A-12345'),
(2, '2025-04-01', 'Pass', 'Tran Duc Bo', '2026-04-01', 'Good condition', '30F-54321'),
(3, '2025-05-01', 'Pass', 'Le Minh Hoang', '2026-05-01', 'Standard check', '51G-99887'),
(4, '2025-06-01', 'Pass', 'Pham Tuan', '2026-06-01', 'Good condition', '43C-11223'),
(5, '2025-07-01', 'Pass', 'Hoang Hai', '2026-07-01', 'Standard check', '60B-33445'),
(6, '2025-08-01', 'Pass', 'Vu Ngoc', '2026-08-01', 'EV check passed', '51H-66778'),
(7, '2025-01-10', 'Pass', 'Dang Khoa', '2026-01-10', 'Standard check', '29C-44556'),
(8, '2025-02-10', 'Pass', 'Bui Viet', '2026-02-10', 'Standard check', '30G-77889'),
(9, '2025-03-10', 'Pass', 'Do Manh', '2026-03-10', 'Luxury check', '51F-13579'),
(10, '2025-04-10', 'Pass', 'Ngo Thang', '2026-04-10', 'SUV check', '61D-24680'),
(11, '2025-05-10', 'Pass', 'Phan Son', '2026-05-10', 'Standard check', '15A-98765'),
(12, '2025-06-10', 'Pass', 'Trinh Long', '2026-06-10', 'Standard check', '37B-55443'),
(13, '2025-07-10', 'Pass', 'Duong Tung', '2026-07-10', 'EV check', '51K-11122'),
(14, '2025-08-10', 'Pass', 'Mai Linh', '2026-08-10', 'Standard check', '29D-33221'),
(15, '2025-09-10', 'Pass', 'Dao Anh', '2026-09-10', 'Commercial check', '30E-99001');

-- 11. TRAFFICVIOLATION
INSERT INTO TRAFFICVIOLATION (Violate_ID, Violation_Type, Violation_Date, Location, Description, Plate_Num) VALUES
(1, 'Speeding', '2026-01-10', 'Phap Van - Cau Gie', 'Exceeded speed limit by 15km/h', '29A-12345'),
(2, 'Red Light', '2026-01-12', 'Ngã tư Láng Hạ', 'Ran a red traffic light', '30F-54321'),
(3, 'Wrong Lane', '2026-01-15', 'Vo Van Kiet', 'Drove in restricted lane', '51G-99887'),
(4, 'Illegal Parking', '2026-01-18', 'Nguyen Hue, D1', 'Parked in a no-parking zone', '43C-11223'),
(5, 'Speeding', '2026-01-20', 'Cao toc Long Thanh', 'Exceeded speed limit by 25km/h', '60B-33445'),
(6, 'No Helmet / Seatbelt', '2026-01-22', 'Cau Giay', 'Driver not wearing seatbelt', '51H-66778'),
(7, 'Wrong Lane', '2026-01-25', 'Thang Long Blvd', 'Improper lane changing', '29C-44556'),
(8, 'Red Light', '2026-01-28', 'Hai Ba Trung', 'Ran a red traffic light', '30G-77889'),
(9, 'Speeding', '2026-02-01', 'Sai Gon Bridge', 'Exceeded speed limit', '51F-13579'),
(10, 'Illegal Parking', '2026-02-03', 'Dong Khoi', 'Double parking', '61D-24680'),
(11, 'Speeding', '2026-02-05', 'Nhat Tan Bridge', 'Exceeded speed limit', '15A-98765'),
(12, 'Red Light', '2026-02-08', 'Giang Vo', 'Ran a red light', '37B-55443'),
(13, 'Wrong Lane', '2026-02-10', 'Nguyen Van Linh', 'Improper turning', '51K-11122'),
(14, 'Illegal Parking', '2026-02-12', 'Ba Trieu', 'Parked on sidewalk', '29D-33221'),
(15, 'Speeding', '2026-02-15', 'Trung Hoa', 'Exceeded speed limit', '30E-99001');

-- 12. FINE
INSERT INTO FINE (Fine_Num, Violate_ID, Amount, Due_Date, Payment_Status, Paid_Date) VALUES
(1, 1, 1000000.00, '2026-02-10', 'Paid', '2026-01-15'),
(2, 2, 2000000.00, '2026-02-12', 'Paid', '2026-01-18'),
(3, 3, 1500000.00, '2026-02-15', 'Unpaid', NULL),
(4, 4, 800000.00, '2026-02-18', 'Paid', '2026-01-20'),
(5, 5, 4000000.00, '2026-02-20', 'Unpaid', NULL),
(6, 6, 500000.00, '2026-02-22', 'Paid', '2026-01-25'),
(7, 7, 1500000.00, '2026-02-25', 'Unpaid', NULL),
(8, 8, 2000000.00, '2026-02-28', 'Paid', '2026-02-01'),
(9, 9, 3000000.00, '2026-03-01', 'Unpaid', NULL),
(10, 10, 800000.00, '2026-03-03', 'Paid', '2026-02-05'),
(11, 11, 1000000.00, '2026-03-05', 'Paid', '2026-02-07'),
(12, 12, 2000000.00, '2026-03-08', 'Unpaid', NULL),
(13, 13, 1500000.00, '2026-03-10', 'Paid', '2026-02-11'),
(14, 14, 800000.00, '2026-03-12', 'Unpaid', NULL),
(15, 15, 2500000.00, '2026-03-15', 'Paid', '2026-02-18');