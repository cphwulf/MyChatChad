package chadchat.domain;

public interface MessageRepository extends MessageFactory{
    Iterable<Message> findSomeMessages(int i);
    Iterable<Message> findAllMessages();
    Iterable<Message> findMessageFrom(int i);
}
