package com.expense.manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "achievements", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "achievement_type"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AchievementType achievementType;

    @Column(nullable = false)
    private String title;

    private String description;

    private String icon;

    private int points;

    @Column(nullable = false)
    private boolean unlocked;

    private LocalDateTime unlockedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (icon == null) icon = "fa-trophy";
        if (points == 0) points = 10;
    }

    public enum AchievementType {
        FIRST_SAVER, WEEK_STREAK, BUDGET_MASTER, GOAL_CRUSHER,
        EXPENSE_CUTTER, FIRST_GOAL, SAVINGS_STREAK, CATEGORY_EXPERT
    }
}
