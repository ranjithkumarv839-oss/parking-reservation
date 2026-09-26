package com.example.parkingreservation.service;

import org.springframework.stereotype.Service;

@Service
public class AdminSettingsService {

    private double basePricePerHour = 20.0;
    private double overtimeRatePerHour = 40.0;
    private int overtimeGracePeriodMinutes = 15;
    private double evChargingPrice = 5.0;
    private int maxBookingHours = 24;
    private double monthlyPermitPrice = 500.0;
    private String adminEmail = "rkpooja425@gmail.com";
    private String adminPassword = "rkdev4215";

    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }

    public String getAdminPassword() { return adminPassword; }
    public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }

    public double getBasePricePerHour() { return basePricePerHour; }
    public void setBasePricePerHour(double basePricePerHour) { this.basePricePerHour = basePricePerHour; }

    public double getOvertimeRatePerHour() { return overtimeRatePerHour; }
    public void setOvertimeRatePerHour(double overtimeRatePerHour) { this.overtimeRatePerHour = overtimeRatePerHour; }

    public int getOvertimeGracePeriodMinutes() { return overtimeGracePeriodMinutes; }
    public void setOvertimeGracePeriodMinutes(int overtimeGracePeriodMinutes) { this.overtimeGracePeriodMinutes = overtimeGracePeriodMinutes; }

    public double getEvChargingPrice() { return evChargingPrice; }
    public void setEvChargingPrice(double evChargingPrice) { this.evChargingPrice = evChargingPrice; }

    public int getMaxBookingHours() { return maxBookingHours; }
    public void setMaxBookingHours(int maxBookingHours) { this.maxBookingHours = maxBookingHours; }

    public double getMonthlyPermitPrice() { return monthlyPermitPrice; }
    public void setMonthlyPermitPrice(double monthlyPermitPrice) { this.monthlyPermitPrice = monthlyPermitPrice; }
}
