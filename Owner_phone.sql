create table Owner_phone(
Owner_ID int NOT NULL,
Phone varchar(20) NOT NULL,
primary key(Owner_ID),
foreign key(Owner_ID) references Owner(Owner_ID)
);