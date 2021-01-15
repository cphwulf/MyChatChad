package chadchat.domain;

import java.time.LocalDateTime;

public interface MessageFactory {
    Message createMessage(int userID, String msg, LocalDateTime time);
}
