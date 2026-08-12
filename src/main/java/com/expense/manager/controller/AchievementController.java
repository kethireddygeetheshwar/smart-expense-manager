package com.expense.manager.controller;

import com.expense.manager.dto.response.AchievementResponse;
import com.expense.manager.entity.Achievement;
import com.expense.manager.entity.User;
import com.expense.manager.repository.AchievementRepository;
import com.expense.manager.repository.UserRepository;
import com.expense.manager.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementRepository achievementRepository;
    private final UserRepository userRepository;
    private final UserContext userContext;

    @GetMapping
    public ResponseEntity<List<AchievementResponse>> getAchievements() {
        ensureAchievementsExist();
        List<Achievement> achievements = achievementRepository.findByUserIdOrderByCreatedAtDesc(userContext.getCurrentUserId());
        return ResponseEntity.ok(achievements.stream().map(AchievementResponse::fromEntity).collect(Collectors.toList()));
    }

    @GetMapping("/unlocked")
    public ResponseEntity<List<AchievementResponse>> getUnlocked() {
        ensureAchievementsExist();
        List<Achievement> achievements = achievementRepository.findByUserIdAndUnlockedTrue(userContext.getCurrentUserId());
        return ResponseEntity.ok(achievements.stream().map(AchievementResponse::fromEntity).collect(Collectors.toList()));
    }

    private void ensureAchievementsExist() {
        Long userId = userContext.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow();
        for (Achievement.AchievementType type : Achievement.AchievementType.values()) {
            if (!achievementRepository.existsByUserIdAndAchievementType(userId, type)) {
                Achievement achievement = Achievement.builder()
                        .user(user)
                        .achievementType(type)
                        .title(getTitle(type))
                        .description(getDescription(type))
                        .icon(getIcon(type))
                        .points(getPoints(type))
                        .unlocked(false)
                        .build();
                achievementRepository.save(achievement);
            }
        }
    }

    private String getTitle(Achievement.AchievementType type) {
        return switch (type) {
            case FIRST_SAVER -> "First Saver";
            case WEEK_STREAK -> "7-Day Streak";
            case BUDGET_MASTER -> "Budget Master";
            case GOAL_CRUSHER -> "Goal Crusher";
            case EXPENSE_CUTTER -> "Expense Cutter";
            case FIRST_GOAL -> "Dream Big";
            case SAVINGS_STREAK -> "Savings Streak";
            case CATEGORY_EXPERT -> "Category Expert";
        };
    }

    private String getDescription(Achievement.AchievementType type) {
        return switch (type) {
            case FIRST_SAVER -> "Saved ₹1,000 for the first time";
            case WEEK_STREAK -> "Stayed under daily budget for 7 days";
            case BUDGET_MASTER -> "Stayed within budget for 3 months";
            case GOAL_CRUSHER -> "Completed a financial goal";
            case EXPENSE_CUTTER -> "Reduced monthly expenses by 10%";
            case FIRST_GOAL -> "Created your first financial goal";
            case SAVINGS_STREAK -> "Saved money 3 months in a row";
            case CATEGORY_EXPERT -> "Tracked expenses in all categories";
        };
    }

    private String getIcon(Achievement.AchievementType type) {
        return switch (type) {
            case FIRST_SAVER -> "fa-piggy-bank";
            case WEEK_STREAK -> "fa-fire";
            case BUDGET_MASTER -> "fa-crown";
            case GOAL_CRUSHER -> "fa-bullseye";
            case EXPENSE_CUTTER -> "fa-scissors";
            case FIRST_GOAL -> "fa-star";
            case SAVINGS_STREAK -> "fa-chart-line";
            case CATEGORY_EXPERT -> "fa-layer-group";
        };
    }

    private int getPoints(Achievement.AchievementType type) {
        return switch (type) {
            case FIRST_SAVER, FIRST_GOAL -> 10;
            case WEEK_STREAK, EXPENSE_CUTTER -> 25;
            case BUDGET_MASTER, GOAL_CRUSHER -> 50;
            case SAVINGS_STREAK, CATEGORY_EXPERT -> 30;
        };
    }
}
