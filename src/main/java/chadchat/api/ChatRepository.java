package chadchat.api;

import chadchat.domain.MessageRepository;
import chadchat.domain.UserFactory;
import chadchat.domain.UserRepository;

public interface ChatRepository extends UserRepository, MessageRepository {
}
