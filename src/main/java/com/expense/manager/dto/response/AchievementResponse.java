package com.expense.manager.dto.response;

import com.expense.manager.entity.Achievement;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AchievementResponse {
    private Long id;
    private String type;
    private String title;
    private String description;
    private String icon;
    private int points;
    private boolean unlocked;

    public static AchievementResponse fromEntity(Achievement a) {
        return AchievementResponse.builder()
                .id(a.getId())
                .type(a.getAchievementType() != null ? a.getAchievementType().name() : "UNKNOWN")
                .title(a.getTitle())
                .description(a.getDescription())
                .icon(a.getIcon())
                .points(a.getPoints())
                .unlocked(a.isUnlocked())
                .build();
    }
}
