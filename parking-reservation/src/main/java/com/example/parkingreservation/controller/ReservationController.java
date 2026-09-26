package com.example.parkingreservation.controller;

import com.example.parkingreservation.model.ParkingSlot;
import com.example.parkingreservation.model.Reservation;
import com.example.parkingreservation.model.User;
import com.example.parkingreservation.repository.ParkingSlotRepository;
import com.example.parkingreservation.repository.ReservationRepository;
import com.example.parkingreservation.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.context.annotation.PropertySource;
import org.springframework.beans.factory.annotation.Value;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    private final ParkingSlotRepository slotRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final com.example.parkingreservation.service.AdminSettingsService settingsService;

    public ReservationController(ParkingSlotRepository slotRepository,
                                 ReservationRepository reservationRepository,
                                 UserRepository userRepository,
                                 com.example.parkingreservation.service.AdminSettingsService settingsService) {
        this.slotRepository = slotRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.settingsService = settingsService;
    }

    // ==========================
    // Show Booking Page
    // ==========================
    @GetMapping("/book/{id}")
    public String bookForm(@PathVariable Long id, Model model) {
        List<ParkingSlot> slots = slotRepository.findAll();
        PageController.calculateDynamicPrices(slots, settingsService.getEvChargingPrice());
        ParkingSlot slot = slots.stream().filter(s -> s.getId().equals(id)).findFirst()
                .orElseThrow(() -> new RuntimeException("Parking slot not found"));

        model.addAttribute("slot", slot);
        return "book";
    }

    // ==========================
    // Confirm Booking
    // ==========================
    @PostMapping("/book/{id}")
    public String book(@PathVariable Long id,
                       @RequestParam String startTime,
                       @RequestParam String endTime,
                       @RequestParam(defaultValue = "false") boolean needValet,
                       @RequestParam String licensePlate,
                       @RequestParam(defaultValue = "false") boolean disabilityConfirmed,
                       Authentication authentication,
                       Model model) {

        List<ParkingSlot> slots = slotRepository.findAll();
        PageController.calculateDynamicPrices(slots, settingsService.getEvChargingPrice());
        ParkingSlot slot = slots.stream().filter(s -> s.getId().equals(id)).findFirst()
                .orElseThrow(() -> new RuntimeException("Parking slot not found"));

        if (!"AVAILABLE".equals(slot.getStatus())) {
            model.addAttribute("slot", slot);
            model.addAttribute("errorMessage",
                    "Sorry! This parking slot has already been booked.");
            return "book";
        }

        // License plate validation (alphanumeric, 4 to 15 chars)
        String cleanPlate = licensePlate.trim().toUpperCase();
        if (!cleanPlate.matches("^[A-Z0-9 -]{4,15}$")) {
            model.addAttribute("slot", slot);
            model.addAttribute("errorMessage",
                    "Invalid Vehicle Number. Please enter a valid registration (e.g. KA-03-MK-7382).");
            return "book";
        }

        // Disability slot eligibility check
        if (slot.isDisabilitySlot() && !disabilityConfirmed) {
            model.addAttribute("slot", slot);
            model.addAttribute("errorMessage",
                    "You must confirm eligibility to reserve a priority accessible slot.");
            return "book";
        }

        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);

        if (!end.isAfter(start)) {
            model.addAttribute("slot", slot);
            model.addAttribute("errorMessage",
                    "End time must be after Start time.");
            return "book";
        }

        long bookingHours = java.time.Duration.between(start, end).toHours();
        if (bookingHours > settingsService.getMaxBookingHours()) {
            model.addAttribute("slot", slot);
            model.addAttribute("errorMessage",
                    "Booking duration cannot exceed the maximum allowed limit of " + settingsService.getMaxBookingHours() + " hours.");
            return "book";
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Reservation reservation = new Reservation();

        reservation.setSlot(slot);
        reservation.setUser(user);
        reservation.setStartTime(start);
        reservation.setEndTime(end);
        reservation.setBookedExitTime(end); // Save booked exit time
        reservation.setStatus("ACTIVE");
        reservation.setNeedValet(needValet);
        reservation.setLicensePlate(cleanPlate);
        reservation.setBookedPricePerHour(slot.getCurrentPrice()); // Lock current dynamic price

        reservationRepository.save(reservation);

        slot.setStatus("OCCUPIED");
        slotRepository.save(slot);

        model.addAttribute("reservation", reservation);

        return "booking-confirmation";
    }

    // ==========================
    // View My Reservations
    // ==========================
    @GetMapping("/my")
    public String myReservations(Authentication authentication,
                                 Model model) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Reservation> reservations =
                reservationRepository.findByUserOrderByStartTimeDesc(user);

        model.addAttribute("reservations", reservations);

        return "my-reservations";
    }

    // ==========================
    // Cancel Reservation
    // ==========================
    @PostMapping("/cancel/{id}")
    public String cancelReservation(@PathVariable Long id,
                                    Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        // Prevent users from cancelling other users' reservations
        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied.");
        }

        reservation.setStatus("CANCELLED");
        reservationRepository.save(reservation);

        ParkingSlot slot = reservation.getSlot();
        slot.setStatus("AVAILABLE");
        slotRepository.save(slot);

        return "redirect:/reservations/my";
    }

    // ==========================
    // Checkout / Mark Collected Form
    // ==========================
    @GetMapping("/checkout/{id}")
    public String checkoutForm(@PathVariable Long id,
                               @RequestParam(required = false) String simulateExitTime,
                               Authentication authentication,
                               Model model) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied.");
        }

        LocalDateTime actualExit = LocalDateTime.now();
        if (simulateExitTime != null && !simulateExitTime.isEmpty()) {
            actualExit = LocalDateTime.parse(simulateExitTime);
        }

        // Calculate overtime
        double rate = settingsService.getOvertimeRatePerHour();
        int grace = settingsService.getOvertimeGracePeriodMinutes();

        double overtimeCharge = 0.0;
        long extraMinutes = 0;
        if (actualExit.isAfter(reservation.getBookedExitTime())) {
            extraMinutes = java.time.Duration.between(reservation.getBookedExitTime(), actualExit).toMinutes();
            if (extraMinutes > grace) {
                long hours = (long) Math.ceil((double) extraMinutes / 60.0);
                overtimeCharge = hours * rate;
            }
        }

        double baseCost = reservation.getTotalCost();
        double valetCost = reservation.isNeedValet() ? 50.0 : 0.0;
        double totalPaid = baseCost + overtimeCharge + valetCost;

        model.addAttribute("reservation", reservation);
        model.addAttribute("actualExitTime", actualExit);
        model.addAttribute("extraMinutes", extraMinutes);
        model.addAttribute("overtimeCharge", overtimeCharge);
        model.addAttribute("totalPaid", totalPaid);

        return "checkout";
    }

    // ==========================
    // Confirm Checkout
    // ==========================
    @PostMapping("/checkout/{id}")
    public String confirmCheckout(@PathVariable Long id,
                                  @RequestParam String actualExitTime,
                                  Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied.");
        }

        LocalDateTime actualExit = LocalDateTime.parse(actualExitTime);

        // Calculate overtime
        double rate = settingsService.getOvertimeRatePerHour();
        int grace = settingsService.getOvertimeGracePeriodMinutes();

        double overtimeCharge = 0.0;
        if (actualExit.isAfter(reservation.getBookedExitTime())) {
            long extraMinutes = java.time.Duration.between(reservation.getBookedExitTime(), actualExit).toMinutes();
            if (extraMinutes > grace) {
                long hours = (long) Math.ceil((double) extraMinutes / 60.0);
                overtimeCharge = hours * rate;
            }
        }

        double baseCost = reservation.getTotalCost();
        double valetCost = reservation.isNeedValet() ? 50.0 : 0.0;
        double totalPaid = baseCost + overtimeCharge + valetCost;

        reservation.setActualExitTime(actualExit);
        reservation.setOvertimeCharge(overtimeCharge);
        reservation.setTotalPaid(totalPaid);
        reservation.setStatus("COMPLETED");
        reservationRepository.save(reservation);

        ParkingSlot slot = reservation.getSlot();
        slot.setStatus("AVAILABLE");
        slotRepository.save(slot);

        return "redirect:/reservations/my?checkoutSuccess";
    }
}