package com.example.parkingreservation.controller;

import com.example.parkingreservation.repository.ParkingSlotRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class SlotController {
    private final ParkingSlotRepository slotRepository;
    private final com.example.parkingreservation.service.AdminSettingsService settingsService;
    public SlotController(ParkingSlotRepository slotRepository,
                          com.example.parkingreservation.service.AdminSettingsService settingsService) {
        this.slotRepository = slotRepository;
        this.settingsService = settingsService;
    }
    @GetMapping("/slots")
    public String listSlots(Model model) {
        java.util.List<com.example.parkingreservation.model.ParkingSlot> slots = slotRepository.findAll();
        PageController.calculateDynamicPrices(slots, settingsService.getEvChargingPrice());
        model.addAttribute("slots", slots);
        return "slots";
    }
}