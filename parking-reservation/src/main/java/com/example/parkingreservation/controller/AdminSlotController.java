package com.example.parkingreservation.controller;

import com.example.parkingreservation.model.ParkingSlot;
import com.example.parkingreservation.repository.ParkingSlotRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.example.parkingreservation.model.Reservation;
import com.example.parkingreservation.repository.ReservationRepository;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/slots")
public class AdminSlotController {

    private final ParkingSlotRepository slotRepository;
    private final ReservationRepository reservationRepository;

    public AdminSlotController(ParkingSlotRepository slotRepository,
                               ReservationRepository reservationRepository) {
        this.slotRepository = slotRepository;
        this.reservationRepository = reservationRepository;
    }

    @GetMapping
    public String list(Model model) {
        List<ParkingSlot> slots = slotRepository.findAll();
        long total = slots.size();
        long occupied = slots.stream().filter(s -> "OCCUPIED".equals(s.getStatus())).count();
        long available = total - occupied;

        List<Reservation> allReservations = reservationRepository.findAll();
        List<Reservation> overtimeReservations = allReservations.stream()
                .filter(r -> r.getOvertimeCharge() != null && r.getOvertimeCharge() > 0)
                .collect(Collectors.toList());

        double totalOvertimeCollected = allReservations.stream()
                .filter(r -> r.getOvertimeCharge() != null)
                .mapToDouble(Reservation::getOvertimeCharge)
                .sum();

        model.addAttribute("slots", slots);
        model.addAttribute("totalSlots", total);
        model.addAttribute("occupiedCount", occupied);
        model.addAttribute("availableCount", available);
        
        model.addAttribute("overtimeReservations", overtimeReservations);
        model.addAttribute("totalOvertimeCollected", totalOvertimeCollected);

        return "admin/slots";
    }
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("slot", new ParkingSlot());
        return "admin/slot-form";
    }
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        ParkingSlot slot = slotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Slot not found"));
        model.addAttribute("slot", slot);
        return "admin/slot-form";
    }
    @PostMapping("/save")
    public String save(@ModelAttribute ParkingSlot slot,
                       @RequestParam String lotName,
                       @RequestParam String parkingFloor) {
        slot.setLocation(lotName + " - " + parkingFloor);
        slotRepository.save(slot);
        return "redirect:/admin/slots";
    }
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        slotRepository.deleteById(id);
        return "redirect:/admin/slots";
    }
}