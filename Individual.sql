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