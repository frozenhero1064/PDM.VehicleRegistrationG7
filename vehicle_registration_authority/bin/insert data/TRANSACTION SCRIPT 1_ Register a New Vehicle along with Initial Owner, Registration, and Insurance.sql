-- TRANSACTION SCRIPT 1: Register a New Vehicle along with Initial Owner, Registration, and Insurance
USE VehicleManagement;
DROP PROCEDURE IF EXISTS sp_RegisterNewVehicle;
DELIMITER //

CREATE PROCEDURE sp_RegisterNewVehicle(
    IN p_PlateNum VARCHAR(20),
    IN p_ChassisNum VARCHAR(50),
    IN p_EngineNum VARCHAR(50),
    IN p_Brand VARCHAR(50),
    IN p_Model VARCHAR(50),
    IN p_YOM INT,
    IN p_Color VARCHAR(30),
    IN p_Type VARCHAR(30),
    IN p_OwnerID INT,
    IN p_RegID INT,
    IN p_PolicyNum VARCHAR(30)
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Transaction failed: Error registering new vehicle.';
    END;

    START TRANSACTION;

        -- 1. Insert Vehicle
        INSERT INTO VEHICLE (Plate_Num, Chassis_Num, Engine_Num, Brand, Model, YOM, Color, Type, Previous_Plate_Num)
        VALUES (p_PlateNum, p_ChassisNum, p_EngineNum, p_Brand, p_Model, p_YOM, p_Color, p_Type, NULL);

        -- 2. Link Ownership
        INSERT INTO Ownership (Ownership_ID, Owner_ID, Start_Date, End_Date, Transfer_Type)
        VALUES (FLOOR(RAND() * 900000 + 100000), p_OwnerID, CURDATE(), NULL, 'New Purchase');

        -- 3. Insert Registration
        INSERT INTO REGISTRATION (Registration_ID, Registration_Date, Expiry_Date, Status, Issued_By, Plate_Num)
        VALUES (p_RegID, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 10 YEAR), 'Active', 'Department of Transport', p_PlateNum);

        -- 4. Insert Insurance Policy
        INSERT INTO INSURANCEPOLICY (Policy_Num, Policy_Name, Start_Date, End_Date, Insurance_Type, Plate_Num)
        VALUES (p_PolicyNum, 'Standard Compulsory Insurance', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 YEAR), 'Compulsory', p_PlateNum);

    COMMIT;
END //

DELIMITER ;
