package com.example.parkingreservation.model;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "slot_id", nullable = false)
    private ParkingSlot slot;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(nullable = false)
    private boolean needValet = false;

    @Column(nullable = true)
    private String licensePlate;

    @Column(nullable = true)
    private Double bookedPricePerHour;

    @Column(nullable = true)
    private LocalDateTime bookedExitTime;

    @Column(nullable = true)
    private LocalDateTime actualExitTime;

    @Column(nullable = false)
    private Double overtimeCharge = 0.0;

    @Column(nullable = false)
    private Double totalPaid = 0.0;

    public Reservation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ParkingSlot getSlot() { return slot; }
    public void setSlot(ParkingSlot slot) { this.slot = slot; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isNeedValet() { return needValet; }
    public void setNeedValet(boolean needValet) { this.needValet = needValet; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public Double getBookedPricePerHour() { return bookedPricePerHour; }
    public void setBookedPricePerHour(Double bookedPricePerHour) { this.bookedPricePerHour = bookedPricePerHour; }

    public LocalDateTime getBookedExitTime() { return bookedExitTime; }
    public void setBookedExitTime(LocalDateTime bookedExitTime) { this.bookedExitTime = bookedExitTime; }

    public LocalDateTime getActualExitTime() { return actualExitTime; }
    public void setActualExitTime(LocalDateTime actualExitTime) { this.actualExitTime = actualExitTime; }

    public Double getOvertimeCharge() { return overtimeCharge; }
    public void setOvertimeCharge(Double overtimeCharge) { this.overtimeCharge = overtimeCharge; }

    public Double getTotalPaid() { return totalPaid; }
    public void setTotalPaid(Double totalPaid) { this.totalPaid = totalPaid; }

    public long getDurationHours() {
        if (startTime == null || endTime == null) return 0;
        return Duration.between(startTime, endTime).toHours();
    }

    public double getTotalCost() {
        if (slot == null) return 0;
        double rate = bookedPricePerHour != null ? bookedPricePerHour : slot.getPricePerHour();
        return getDurationHours() * rate;
    }
}