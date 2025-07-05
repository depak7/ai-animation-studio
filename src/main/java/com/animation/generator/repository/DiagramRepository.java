package com.animation.generator.repository;

import com.animation.generator.objects.Diagram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagramRepository extends JpaRepository<Diagram,Long> {
    List<Diagram> findByChatId(Long chatId);
    List<Diagram> findByUserIdAndChatId(Long userId, Long chatId);
    List<Diagram> findByGuestIdAndChatId(String guestId, Long chatId);
    List<Diagram> findByGuestId(String guestId);
}
