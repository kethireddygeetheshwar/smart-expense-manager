package com.expense.manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal targetAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal savedAmount;

    private LocalDate targetDate;

    private String icon;

    @Column(nullable = false)
    private boolean completed;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (savedAmount == null) savedAmount = BigDecimal.ZERO;
        if (icon == null) icon = "fa-bullseye";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public double getProgressPercentage() {
        if (targetAmount.compareTo(BigDecimal.ZERO) == 0) return 0;
        return savedAmount.multiply(BigDecimal.valueOf(100))
                .divide(targetAmount, 2, java.math.RoundingMode.HALF_UP).doubleValue();
    }

    public BigDecimal getRemainingAmount() {
        return targetAmount.subtract(savedAmount).max(BigDecimal.ZERO);
    }

    public int getMonthsRemaining() {
        if (targetDate == null) return 12;
        LocalDate now = LocalDate.now();
        return Math.max(1, (targetDate.getYear() - now.getYear()) * 12 + (targetDate.getMonthValue() - now.getMonthValue()));
    }

    public BigDecimal getRecommendedMonthlySaving() {
        return getRemainingAmount().divide(BigDecimal.valueOf(getMonthsRemaining()), 2, java.math.RoundingMode.HALF_UP);
    }
}
