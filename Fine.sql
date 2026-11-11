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
