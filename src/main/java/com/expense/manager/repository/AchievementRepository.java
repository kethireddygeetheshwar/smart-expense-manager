package com.expense.manager.repository;

import com.expense.manager.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Achievement> findByUserIdAndUnlockedTrue(Long userId);
    Optional<Achievement> findByUserIdAndAchievementType(Long userId, Achievement.AchievementType type);
    boolean existsByUserIdAndAchievementType(Long userId, Achievement.AchievementType type);
}
