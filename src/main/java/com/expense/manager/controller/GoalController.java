package com.expense.manager.controller;

import com.expense.manager.dto.request.GoalRequest;
import com.expense.manager.dto.response.GoalResponse;
import com.expense.manager.entity.Goal;
import com.expense.manager.entity.User;
import com.expense.manager.exception.ResourceNotFoundException;
import com.expense.manager.repository.GoalRepository;
import com.expense.manager.repository.UserRepository;
import com.expense.manager.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final UserContext userContext;

    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(@RequestBody GoalRequest request) {
        User user = userRepository.findById(userContext.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Goal goal = Goal.builder()
                .name(request.getName())
                .description(request.getDescription())
                .targetAmount(request.getTargetAmount())
                .targetDate(request.getTargetDate())
                .icon(request.getIcon())
                .user(user)
                .completed(false)
                .build();
        goal = goalRepository.save(goal);
        return ResponseEntity.ok(GoalResponse.fromEntity(goal));
    }

    @GetMapping
    public ResponseEntity<List<GoalResponse>> getGoals() {
        List<Goal> goals = goalRepository.findByUserIdOrderByCreatedAtDesc(userContext.getCurrentUserId());
        return ResponseEntity.ok(goals.stream().map(GoalResponse::fromEntity).collect(Collectors.toList()));
    }

    @PostMapping("/{id}/contribute")
    public ResponseEntity<GoalResponse> contribute(@PathVariable Long id, @RequestParam BigDecimal amount) {
        Goal goal = goalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        goal.setSavedAmount(goal.getSavedAmount().add(amount));
        if (goal.getSavedAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setCompleted(true);
        }
        goal = goalRepository.save(goal);
        return ResponseEntity.ok(GoalResponse.fromEntity(goal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long id) {
        goalRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
