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