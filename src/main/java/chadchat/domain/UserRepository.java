package chadchat.domain;

public interface UserRepository extends UserFactory {
    Iterable<User> findAllUsers();
    User findUser(String name);
    User findUser(int id);
}
