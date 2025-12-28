package com.fastfoodmanager.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_closures")
public class CashClosure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String operatorUsername;

    @Column(nullable = false)
    private LocalDate businessDate;

    @Column(nullable = false)
    private LocalDateTime periodStart;

    @Column(nullable = false)
    private LocalDateTime periodEnd;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDateTime closedAt;

    public CashClosure() {}

    public CashClosure(String operatorUsername,
                       LocalDate businessDate,
                       LocalDateTime periodStart,
                       LocalDateTime periodEnd,
                       Double amount,
                       LocalDateTime closedAt) {
        this.operatorUsername = operatorUsername;
        this.businessDate = businessDate;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.amount = amount;
        this.closedAt = closedAt;
    }

    public Long getId() { return id; }

    public String getOperatorUsername() { return operatorUsername; }
    public void setOperatorUsername(String operatorUsername) { this.operatorUsername = operatorUsername; }

    public LocalDate getBusinessDate() { return businessDate; }
    public void setBusinessDate(LocalDate businessDate) { this.businessDate = businessDate; }

    public LocalDateTime getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDateTime periodStart) { this.periodStart = periodStart; }

    public LocalDateTime getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDateTime periodEnd) { this.periodEnd = periodEnd; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
}
