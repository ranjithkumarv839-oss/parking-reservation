package com.example.parkingreservation.controller;

import com.example.parkingreservation.model.ParkingSlot;
import com.example.parkingreservation.model.Reservation;
import com.example.parkingreservation.model.User;
import com.example.parkingreservation.model.MonthlyPermit;
import com.example.parkingreservation.repository.ParkingSlotRepository;
import com.example.parkingreservation.repository.ReservationRepository;
import com.example.parkingreservation.repository.UserRepository;
import com.example.parkingreservation.repository.MonthlyPermitRepository;
import com.example.parkingreservation.service.AdminSettingsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final ParkingSlotRepository slotRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final MonthlyPermitRepository permitRepository;
    private final AdminSettingsService settingsService;

    public AdminDashboardController(ParkingSlotRepository slotRepository,
                                    ReservationRepository reservationRepository,
                                    UserRepository userRepository,
                                    MonthlyPermitRepository permitRepository,
                                    AdminSettingsService settingsService) {
        this.slotRepository = slotRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.permitRepository = permitRepository;
        this.settingsService = settingsService;
    }

    @GetMapping("/login")
    public String loginForm() {
        return "admin/login";
    }

    @PostMapping("/login")
    public String loginPost(@RequestParam String email,
                            @RequestParam String password,
                            HttpServletRequest request,
                            Model model) {
        String trimmedEmail = email.trim();
        java.util.Optional<User> userOpt = userRepository.findByEmail(trimmedEmail);
        if (userOpt.isPresent() && !"ROLE_ADMIN".equals(userOpt.get().getRole())) {
            model.addAttribute("errorMessage", "This account is not an administrator account.");
            return "admin/login";
        }
        if (settingsService.getAdminEmail().equalsIgnoreCase(trimmedEmail) && settingsService.getAdminPassword().equals(password)) {
            UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(
                    email, null, AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
            SecurityContext sc = SecurityContextHolder.getContext();
            sc.setAuthentication(authReq);
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);
            return "redirect:/admin/dashboard";
        } else {
            model.addAttribute("errorMessage", "Invalid admin credentials.");
            return "admin/login";
        }
    }

    // ========================================
    // Admin Dashboard
    // ========================================
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<ParkingSlot> slots = slotRepository.findAll();
        List<Reservation> reservations = reservationRepository.findAll();
        List<User> users = userRepository.findAll();
        List<MonthlyPermit> permits = permitRepository.findAll();

        long totalSlots = slots.size();
        long availableSlots = slots.stream().filter(s -> "AVAILABLE".equals(s.getStatus())).count();
        long occupiedSlots = slots.stream().filter(s -> "OCCUPIED".equals(s.getStatus())).count();
        long reservedSlots = slots.stream().filter(s -> "RESERVED".equals(s.getStatus())).count();
        long evSlots = slots.stream().filter(ParkingSlot::isEvCharging).count();
        long disabledSlots = slots.stream().filter(ParkingSlot::isDisabilitySlot).count();
        long maintenanceSlots = slots.stream().filter(s -> "UNDER_MAINTENANCE".equals(s.getStatus())).count();

        long monthlyPermitsCount = permits.stream().filter(p -> "ACTIVE".equals(p.getStatus())).count();
        
        LocalDate today = LocalDate.now();
        long todayBookings = reservations.stream()
                .filter(r -> r.getStartTime().toLocalDate().isEqual(today))
                .count();

        long activeReservations = reservations.stream().filter(r -> "ACTIVE".equals(r.getStatus())).count();
        long completedReservations = reservations.stream().filter(r -> "COMPLETED".equals(r.getStatus())).count();
        long cancelledReservations = reservations.stream().filter(r -> "CANCELLED".equals(r.getStatus())).count();

        long totalUsers = users.size();

        double totalRevenue = reservations.stream()
                .filter(r -> "COMPLETED".equals(r.getStatus()))
                .mapToDouble(r -> r.getTotalPaid())
                .sum();

        double overtimeCollected = reservations.stream()
                .filter(r -> "COMPLETED".equals(r.getStatus()))
                .mapToDouble(Reservation::getOvertimeCharge)
                .sum();

        long valetBookings = reservations.stream().filter(Reservation::isNeedValet).count();

        double occupancyPercentage = totalSlots == 0 ? 0 : ((double) (totalSlots - availableSlots) / totalSlots) * 100.0;

        model.addAttribute("totalSlots", totalSlots);
        model.addAttribute("availableSlots", availableSlots);
        model.addAttribute("occupiedSlots", occupiedSlots);
        model.addAttribute("reservedSlots", reservedSlots);
        model.addAttribute("evSlots", evSlots);
        model.addAttribute("disabledSlots", disabledSlots);
        model.addAttribute("maintenanceSlots", maintenanceSlots);
        model.addAttribute("monthlyPermitsCount", monthlyPermitsCount);
        model.addAttribute("todayBookings", todayBookings);
        model.addAttribute("activeReservations", activeReservations);
        model.addAttribute("completedReservations", completedReservations);
        model.addAttribute("cancelledReservations", cancelledReservations);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("overtimeCollected", overtimeCollected);
        model.addAttribute("valetBookings", valetBookings);
        model.addAttribute("occupancyPercentage", Math.round(occupancyPercentage * 10.0) / 10.0);

        return "admin/dashboard";
    }

    // ========================================
    // Live Status API for Dynamic Board Reload
    // ========================================
    @GetMapping("/dashboard/live-stats")
    @ResponseBody
    public java.util.Map<String, Object> liveStats() {
        List<ParkingSlot> slots = slotRepository.findAll();
        long totalSlots = slots.size();
        long availableSlots = slots.stream().filter(s -> "AVAILABLE".equals(s.getStatus())).count();
        long occupiedSlots = slots.stream().filter(s -> "OCCUPIED".equals(s.getStatus())).count();
        long reservedSlots = slots.stream().filter(s -> "RESERVED".equals(s.getStatus())).count();
        long evSlots = slots.stream().filter(ParkingSlot::isEvCharging).count();
        long disabledSlots = slots.stream().filter(ParkingSlot::isDisabilitySlot).count();
        long maintenanceSlots = slots.stream().filter(s -> "UNDER_MAINTENANCE".equals(s.getStatus())).count();
        double occupancyPercentage = totalSlots == 0 ? 0 : ((double) (totalSlots - availableSlots) / totalSlots) * 100.0;

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalSlots", totalSlots);
        stats.put("availableSlots", availableSlots);
        stats.put("occupiedSlots", occupiedSlots);
        stats.put("reservedSlots", reservedSlots);
        stats.put("evSlots", evSlots);
        stats.put("disabledSlots", disabledSlots);
        stats.put("maintenanceSlots", maintenanceSlots);
        stats.put("occupancyPercentage", Math.round(occupancyPercentage * 10.0) / 10.0);
        stats.put("totalUsers", userRepository.count());
        return stats;
    }

}
