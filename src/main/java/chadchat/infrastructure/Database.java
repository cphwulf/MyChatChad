package chadchat.infrastructure;

import chadchat.api.ChatRepository;
import chadchat.domain.Message;
import chadchat.domain.User;
import chadchat.domain.UserExists;
import chadchat.domain.UserRepository;
import com.google.protobuf.TimestampProto;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.NoSuchElementException;

public class Database implements ChatRepository {

    // JDBC driver name and database URL
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost/chadchat";


    //  Database credentials
    private static final String USER = "chadchat";

    // Database version
    private static final int version = 3;

    public Database() throws ClassNotFoundException {
        Class.forName(JDBC_DRIVER);
        if (getCurrentVersion() != getVersion()) {
            throw new IllegalStateException("Database in wrong state");
        }
    }

    private Message loadMessage(ResultSet rs) throws SQLException {
        return new Message(
                rs.getInt("message.id"),
                rs.getInt("message.userID"),
                rs.getString("message.message"),
                rs.getTimestamp("message.createdAt").toLocalDateTime());
    }

    private User loadUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("users.id"),
                rs.getString("users.name"),
                rs.getTimestamp("users.createdAt").toLocalDateTime(),
                rs.getBytes("users.salt"),
                rs.getBytes("users.secret"));
    }

    @Override
    public Iterable<User> findAllUsers() {
        try (Connection conn = getConnection()) {
            PreparedStatement s = conn.prepareStatement("SELECT * FROM users;");
            ResultSet rs = s.executeQuery();
            ArrayList<User> items = new ArrayList<>();
            while(rs.next()) {
                items.add(loadUser(rs));
            }
            return items;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User findUser(String name) throws NoSuchElementException {
        try(Connection conn = getConnection()) {
            PreparedStatement s = conn.prepareStatement(
                    "SELECT * FROM users WHERE name = ?;");
            s.setString(1, name);
            ResultSet rs = s.executeQuery();
            if(rs.next()) {
                return loadUser(rs);
            } else {
                System.err.println("No version in properties.");
                throw new NoSuchElementException(name);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User findUser(int id) throws NoSuchElementException {
        try(Connection conn = getConnection()) {
            PreparedStatement s = conn.prepareStatement(
                    "SELECT * FROM users WHERE id = ?;");
            s.setInt(1, id);
            ResultSet rs = s.executeQuery();
            if(rs.next()) {
                return loadUser(rs);
            } else {
                System.err.println("No version in properties.");
                throw new NoSuchElementException("No user with id: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<Message> findSomeMessages(int i) {
        try (Connection conn = getConnection()) {
            PreparedStatement s = conn.prepareStatement("SELECT * FROM message ORDER BY createdAt DESC LIMIT ?;");
            s.setInt(1,i);
            ResultSet rs = s.executeQuery();
            ArrayList<Message> items = new ArrayList<>();
            while(rs.next()) {
                items.add(loadMessage(rs));
            }
            Collections.reverse(items);
            return items;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Iterable<Message> findAllMessages() {
        try (Connection conn = getConnection()) {
            PreparedStatement s = conn.prepareStatement("SELECT * FROM message;");
            ResultSet rs = s.executeQuery();
            ArrayList<Message> items = new ArrayList<>();
            while(rs.next()) {
                items.add(loadMessage(rs));
            }
            return items;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Message findMessage(int id) throws NoSuchElementException {
        try(Connection conn = getConnection()) {
            PreparedStatement s = conn.prepareStatement(
                    "SELECT * FROM message WHERE id = ?;");
            s.setInt(1, id);
            ResultSet rs = s.executeQuery();
            if(rs.next()) {
                return loadMessage(rs);
            } else {
                System.err.println("No version in properties.");
                throw new NoSuchElementException("No msg with id: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Iterable<Message> findMessageFrom(int userID) throws NoSuchElementException {
        try(Connection conn = getConnection()) {
            PreparedStatement s = conn.prepareStatement(
                    "SELECT * FROM message WHERE userID = ?;");
            s.setInt(1, userID);
            ResultSet rs = s.executeQuery();
            ArrayList<Message> items = new ArrayList<>();
            while(rs.next()) {
                items.add(loadMessage(rs));
            }
            return items;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User createUser(String name, byte[] salt, byte[] secret) throws UserExists {
        int id;
        try (Connection conn = getConnection()) {
            var ps =
                    conn.prepareStatement(
                            "INSERT INTO users (name, salt, secret) " +
                                    "VALUE (?,?,?);",
                            Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setBytes(2, salt);
            ps.setBytes(3, secret);
            try {
                ps.executeUpdate();
            } catch (SQLIntegrityConstraintViolationException e) {
                throw new UserExists(name);
            }

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getInt(1);
            } else {
                throw new UserExists(name);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return findUser(id);
    }

    @Override
    public Message createMessage(int userID, String msg, LocalDateTime time){
        int id = 0;
        try (Connection conn = getConnection()) {
            var ps =
                    conn.prepareStatement(
                            "INSERT INTO message (userID, message, createdAt) " +
                                    "VALUE (?,?,?);",
                            Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, userID);
            ps.setString(2, msg);
            ps.setTimestamp(3, Timestamp.valueOf(time));
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getInt(1);
            } else {
                System.out.println("Message does not exist throw");;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return findMessage(id);
    }

    public static int getVersion() {
        return version;
    }

    public static int getCurrentVersion() {
        try (Connection conn = getConnection()) {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT value FROM properties WHERE name = 'version';");
            if(rs.next()) {
                String column = rs.getString("value");
                return Integer.parseInt(column);
            } else {
                System.err.println("No version in properties.");
                return -1;
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return -1;
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, null);
    }
}
