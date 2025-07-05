package com.animation.generator.repository;

import com.animation.generator.objects.Chats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chats, Long> {
    List<Chats> findChatsByUserId(Long userId);
    List<Chats> findChatsByGuestId(String guestId);
}
