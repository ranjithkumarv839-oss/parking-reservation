package com.example.parkingreservation.controller;

import com.example.parkingreservation.model.ParkingSlot;
import com.example.parkingreservation.repository.ParkingSlotRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PageController {

    private final ParkingSlotRepository slotRepository;
    private final com.example.parkingreservation.service.AdminSettingsService settingsService;

    public PageController(ParkingSlotRepository slotRepository,
                          com.example.parkingreservation.service.AdminSettingsService settingsService) {
        this.slotRepository = slotRepository;
        this.settingsService = settingsService;
    }

    public static void calculateDynamicPrices(List<ParkingSlot> slots, double evPrice) {
        long total = slots.size();
        long occupied = slots.stream().filter(s -> "OCCUPIED".equals(s.getStatus())).count();
        double occupancyRate = total == 0 ? 0.0 : (double) occupied / total;
        double pricingFactor = 1.0;
        if (occupancyRate > 0.7) {
            pricingFactor = 1.25;
        } else if (occupancyRate > 0.3) {
            pricingFactor = 1.1;
        }
        for (ParkingSlot slot : slots) {
            double price = slot.getPricePerHour() * pricingFactor;
            if (slot.isEvCharging()) {
                price += evPrice;
            }
            slot.setCurrentPrice(Math.round(price * 100.0) / 100.0);
        }
    }

    @GetMapping("/")
    public String home(Model model) {
        List<ParkingSlot> slots = slotRepository.findAll();
        calculateDynamicPrices(slots, settingsService.getEvChargingPrice());
        long total = slots.size();
        long available = slots.stream().filter(s -> "AVAILABLE".equals(s.getStatus())).count();
        long fillRate = total == 0 ? 0 : Math.round(((double) (total - available) / total) * 100);

        model.addAttribute("totalSlots", total);
        model.addAttribute("fillRate", fillRate);
        return "index";
    }

    @GetMapping("/login")
    public String login(jakarta.servlet.http.HttpServletRequest request, Model model) {
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session != null) {
            String errorMsg = (String) session.getAttribute("loginErrorMessage");
            if (errorMsg != null) {
                model.addAttribute("customError", errorMsg);
                session.removeAttribute("loginErrorMessage");
            }
        }
        return "login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}