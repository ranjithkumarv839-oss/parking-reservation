package com.example.parkingreservation.model;

import jakarta.persistence.*;

@Entity
@Table(name = "parking_slots")
public class ParkingSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String slotNumber;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private Double pricePerHour;

    @Column(nullable = false)
    private String status = "AVAILABLE";

    @Column(nullable = false)
    private boolean evCharging = false;

    @Column(nullable = false)
    private boolean disabilitySlot = false;

    @Transient
    private Double currentPrice;

    public ParkingSlot() {}

    public ParkingSlot(Long id, String slotNumber, String location, Double pricePerHour, String status) {
        this.id = id;
        this.slotNumber = slotNumber;
        this.location = location;
        this.pricePerHour = pricePerHour;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSlotNumber() { return slotNumber; }
    public void setSlotNumber(String slotNumber) { this.slotNumber = slotNumber; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Double getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(Double pricePerHour) { this.pricePerHour = pricePerHour; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isEvCharging() { return evCharging; }
    public void setEvCharging(boolean evCharging) { this.evCharging = evCharging; }

    public boolean isDisabilitySlot() { return disabilitySlot; }
    public void setDisabilitySlot(boolean disabilitySlot) { this.disabilitySlot = disabilitySlot; }

    public Double getCurrentPrice() { return currentPrice != null ? currentPrice : pricePerHour; }
    public void setCurrentPrice(Double currentPrice) { this.currentPrice = currentPrice; }
}