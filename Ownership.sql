create table Ownership(
Ownership_ID int NOT NULL,
Owner_ID int NOT NULL,
Start_Date date,
End_Date date,
Transfer_Type varchar (100),

primary key(Ownership_ID),
foreign key(Owner_ID) references Owner(Owner_ID)
);