package com.expense.manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "budgets", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "category", "month_year"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Expense.Category category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal limitAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal spentAmount;

    @Column(nullable = false)
    private String monthYear;

    @Column(nullable = false)
    private boolean alertSent;

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
        if (spentAmount == null) spentAmount = BigDecimal.ZERO;
        if (!alertSent) alertSent = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public double getUsagePercentage() {
        if (limitAmount.compareTo(BigDecimal.ZERO) == 0) return 0;
        return spentAmount.multiply(BigDecimal.valueOf(100))
                .divide(limitAmount, 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public boolean isNearLimit() {
        return getUsagePercentage() >= 90;
    }

    public boolean isExceeded() {
        return spentAmount.compareTo(limitAmount) > 0;
    }
}
