package com.example.parkingreservation.controller;

import com.example.parkingreservation.model.MonthlyPermit;
import com.example.parkingreservation.model.User;
import com.example.parkingreservation.repository.MonthlyPermitRepository;
import com.example.parkingreservation.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/permits")
public class PermitController {

    private final MonthlyPermitRepository permitRepository;
    private final UserRepository userRepository;

    public PermitController(MonthlyPermitRepository permitRepository, UserRepository userRepository) {
        this.permitRepository = permitRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listPermits(Authentication authentication, Model model) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<MonthlyPermit> permits = permitRepository.findByUserOrderByExpiryDateDesc(user);
        model.addAttribute("permits", permits);
        return "permits";
    }

    @PostMapping("/buy")
    public String buyPermit(@RequestParam String permitType, Authentication authentication, Model model) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if there is already an active permit (expiryDate > today and status = 'ACTIVE')
        Optional<MonthlyPermit> activePermit = permitRepository
                .findFirstByUserAndStatusAndExpiryDateAfter(user, "ACTIVE", LocalDate.now());

        if (activePermit.isPresent()) {
            List<MonthlyPermit> permits = permitRepository.findByUserOrderByExpiryDateDesc(user);
            model.addAttribute("permits", permits);
            model.addAttribute("errorMessage", "Purchase Denied: You already have an active permit (" + activePermit.get().getPermitType() + ") valid until " + activePermit.get().getExpiryDate());
            return "permits";
        }

        MonthlyPermit permit = new MonthlyPermit();
        permit.setUser(user);
        permit.setPermitType(permitType.toUpperCase());
        permit.setStartDate(LocalDate.now());
        permit.setExpiryDate(LocalDate.now().plusMonths(1));
        permit.setStatus("ACTIVE");

        permitRepository.save(permit);

        return "redirect:/permits?success";
    }
}
