create table Owner_email(
Owner_ID int NOT NULL,
email varchar(100) not null unique,
primary key(Owner_ID),
foreign key(Owner_ID) references Owner(Owner_ID)
);