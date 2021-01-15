DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id int PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(25)
);

UPDATE properties
SET value = '1'
WHERE name = "version";