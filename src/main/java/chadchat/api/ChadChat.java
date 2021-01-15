package chadchat.api;

import chadchat.domain.Message;
import chadchat.domain.User;
import chadchat.domain.UserExists;
import chadchat.domain.UserRepository;
import chadchat.infrastructure.Database;

import javax.imageio.plugins.tiff.TIFFImageReadParam;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChadChat {
    private static ChadChat instance;

    public static ChadChat getInstance() {
        if (instance == null) {
            try {
                ChatRepository u = new Database();
                instance = new ChadChat(u);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    private final List<User> activeUsers = new ArrayList<>();
    private final ChatRepository users;
    private final List<MessageNotifier> notifiers = new ArrayList<>();

    private ChadChat(ChatRepository users) {
        this.users = users;
    }

    public User createUser(String name, String password) throws UserExists {
        byte[] salt = User.generateSalt();
        byte[] secret = User.calculateSecret(salt, password);
        return users.createUser(name, salt, secret);
    }

    public void logout(User user){
        activeUsers.remove(user);
    }

    public User login(String name, String password) throws InvalidPassword {
        User user = users.findUser(name);
        if (user.isPasswordCorrect(password)) {
            activeUsers.add(user);
            return user;
        } else  {
            throw new InvalidPassword();
        }
    }

    public void sendMessage(User user, String message){
        Message m = users.createMessage(user.getId(), message, LocalDateTime.now());
        for (MessageNotifier n: notifiers){
            n.notifyNewMessage(m);
        }
    }

    public User findUser(int id){
        return users.findUser(id);
    }

    public void register(MessageNotifier n){
        notifiers.add(n);
    }

    public Iterable<Message> findSomeMessages(int i) {

        return users.findSomeMessages(i);
    }

    public Iterable<Message> findAllMessages() {
        return users.findAllMessages();
    }

    public Iterable<Message> findMessageFrom(int userID) {
        return users.findMessageFrom(userID);
    }

    public Iterable<User> getActiveUsers() {
        return this.activeUsers;
    }

    public interface MessageNotifier {
        void notifyNewMessage(Message m);
    }

    public Iterable<User> getUsers() {
        return users.findAllUsers();
    }
}
