create table Organisation ( 
Owner_ID int NOT NULL,
Company_Name varchar(250) NOT NULL,
Tax_Code varchar(50) UNIQUE,
Represent_Name varchar(250),
primary key(Owner_ID),
foreign key(Owner_ID) references Owner(Owner_ID)
);