Firstly you need to create the database with the following statement 

CREATE TABLE users (
    id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    score INTEGER,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(50),
    PRIMARY KEY (id)
);

and downlaod required libraries:

json-simple-1.1.1.jar
gson-2.1.1.jar

With this server you can clone the following repo and play with you friends 

https://github.com/MahmooudDarwish/Tic-Tac-Toe-Game.git

Team:

Mahmoud Saad

Kareem Ashraf

Mohamed Abdelrahim

Omar Gohary

