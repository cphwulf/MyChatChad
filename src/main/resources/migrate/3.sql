DROP TABLE IF EXISTS message;
CREATE TABLE message (
    id INT PRIMARY KEY AUTO_INCREMENT,
    userID INT,
    message VARCHAR(250) NOT NULL,
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(userID) REFERENCES users(id)
);

UPDATE properties
SET value = '3'
WHERE name = "version";