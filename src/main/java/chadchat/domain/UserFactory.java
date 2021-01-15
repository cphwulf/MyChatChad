package chadchat.domain;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public interface UserFactory {
    User createUser(String name, byte[] salt, byte[] secret) throws UserExists;
}
