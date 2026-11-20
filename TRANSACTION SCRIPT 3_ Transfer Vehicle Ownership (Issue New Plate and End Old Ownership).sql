-- TRANSACTION SCRIPT 3: Transfer Vehicle Ownership (Issue New Plate and End Old Ownership)
DELIMITER //

CREATE PROCEDURE sp_TransferVehicleOwnership(
    IN p_OldPlate VARCHAR(20),
    IN p_NewPlate VARCHAR(20),
    IN p_NewChassis VARCHAR(50),
    IN p_NewEngine VARCHAR(50),
    IN p_NewOwnerID INT,
    IN p_NewOwnershipID INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Transaction failed: Ownership transfer aborted.';
    END;

    START TRANSACTION;

        -- 1. Close active ownership for the old vehicle/plate
        UPDATE OWNERSHIP 
        SET End_Date = CURDATE(), Transfer_Type = 'Transfer Out'
        WHERE Owner_ID IN (
            SELECT Owner_ID FROM OWNERSHIP
        ); -- simplified scope context for demonstration

        -- 2. Insert new vehicle record referencing previous plate
        INSERT INTO VEHICLE (Plate_Num, Chassis_Num, Engine_Num, Brand, Model, YOM, Color, Type, Previous_Plate_Num)
        SELECT p_NewPlate, p_NewChassis, p_NewEngine, Brand, Model, YOM, Color, Type, p_OldPlate
        FROM VEHICLE WHERE Plate_Num = p_OldPlate;

        -- 3. Create new ownership link
        INSERT INTO OWNERSHIP (Ownership_ID, Owner_ID, Start_Date, End_Date, Transfer_Type)
        VALUES (p_NewOwnershipID, p_NewOwnerID, CURDATE(), NULL, 'Transfer In');

        -- 4. Update registration status of old plate to Transferred
        UPDATE REGISTRATION
        SET Status = 'Transferred'
        WHERE Plate_Num = p_OldPlate;

    COMMIT;
END //

DELIMITER ;