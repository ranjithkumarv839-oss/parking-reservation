package com.example.parkingreservation.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "monthly_permits")
public class MonthlyPermit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String permitType; // e.g. STANDARD, PREMIUM, EV

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, EXPIRED, CANCELLED

    public MonthlyPermit() {}

    public MonthlyPermit(Long id, User user, String permitType, LocalDate startDate, LocalDate expiryDate, String status) {
        this.id = id;
        this.user = user;
        this.permitType = permitType;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getPermitType() { return permitType; }
    public void setPermitType(String permitType) { this.permitType = permitType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
