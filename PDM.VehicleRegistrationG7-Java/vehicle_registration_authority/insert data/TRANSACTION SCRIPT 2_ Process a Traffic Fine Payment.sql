-- TRANSACTION SCRIPT 2: Process a Traffic Fine Payment
USE VehicleManagement;
DROP PROCEDURE IF EXISTS sp_PayFine;
DELIMITER //

CREATE PROCEDURE sp_PayFine(
    IN p_FineNum INT,
    IN p_ViolateID INT
)
BEGIN
    DECLARE v_Status VARCHAR(30);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Transaction failed: Could not process fine payment.';
    END;

    START TRANSACTION;

        -- Check current payment status
        SELECT Payment_Status INTO v_Status
        FROM FINE
        WHERE Fine_Num = p_FineNum AND Violate_ID = p_ViolateID
        FOR UPDATE;

        IF v_Status = 'Paid' THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Fine is already paid.';
        ELSE
            UPDATE FINE
            SET Payment_Status = 'Paid', Paid_Date = CURDATE()
            WHERE Fine_Num = p_FineNum AND Violate_ID = p_ViolateID;
        END IF;

    COMMIT;
END //

DELIMITER ;