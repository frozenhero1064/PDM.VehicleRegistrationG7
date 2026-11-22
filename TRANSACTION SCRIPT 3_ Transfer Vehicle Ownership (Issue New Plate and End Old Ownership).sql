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