package com.expense.manager.repository;

import com.expense.manager.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByUserIdOrderByTimestampDesc(Long userId);

    List<ChatMessage> findTop20ByUserIdOrderByTimestampDesc(Long userId);
}
