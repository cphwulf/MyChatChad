package chadchat.domain;

import java.time.LocalDateTime;

public class Message {
    private final int id;
    private final int userID;
    private final String msg;
    private final LocalDateTime time;

    public Message(int id, int userID, String msg, LocalDateTime time) {
        this.id = id;
        this.userID = userID;
        this.msg = msg;
        this.time = time;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", userID=" + userID +
                ", msg='" + msg + '\'' +
                ", time=" + time +
                '}';
    }

    public int getUserID() {
        return userID;
    }

    public String getMsg() {
        return msg;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public int getId() {
        return id;
    }
}
