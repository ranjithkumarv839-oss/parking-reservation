package com.example.parkingreservation.config;

import com.example.parkingreservation.model.ParkingSlot;
import com.example.parkingreservation.model.User;
import com.example.parkingreservation.repository.ParkingSlotRepository;
import com.example.parkingreservation.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ParkingSlotRepository slotRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(ParkingSlotRepository slotRepository,
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (slotRepository.count() == 0) {
            // ParkPoint Main Lot - Level 1
            ParkingSlot ps1 = new ParkingSlot(null, "A-12", "ParkPoint Main Lot - Level 1", 20.0, "AVAILABLE");
            ps1.setEvCharging(true);
            slotRepository.save(ps1);

            ParkingSlot ps2 = new ParkingSlot(null, "A-13", "ParkPoint Main Lot - Level 1", 20.0, "AVAILABLE");
            slotRepository.save(ps2);

            ParkingSlot ps3 = new ParkingSlot(null, "A-14", "ParkPoint Main Lot - Level 1", 20.0, "AVAILABLE");
            slotRepository.save(ps3);

            // ParkPoint Main Lot - Level 2
            ParkingSlot ps4 = new ParkingSlot(null, "B-04", "ParkPoint Main Lot - Level 2", 25.0, "AVAILABLE");
            ps4.setEvCharging(true);
            slotRepository.save(ps4);

            ParkingSlot ps5 = new ParkingSlot(null, "B-05", "ParkPoint Main Lot - Level 2", 25.0, "AVAILABLE");
            ps5.setEvCharging(true);
            slotRepository.save(ps5);

            ParkingSlot ps6 = new ParkingSlot(null, "B-06", "ParkPoint Main Lot - Level 2", 25.0, "AVAILABLE");
            slotRepository.save(ps6);

            // ParkPoint Main Lot - Ground
            ParkingSlot ps7 = new ParkingSlot(null, "C-01", "ParkPoint Main Lot - Ground", 15.0, "AVAILABLE");
            ps7.setDisabilitySlot(true);
            slotRepository.save(ps7);

            ParkingSlot ps8 = new ParkingSlot(null, "C-02", "ParkPoint Main Lot - Ground", 15.0, "AVAILABLE");
            ps8.setDisabilitySlot(true);
            slotRepository.save(ps8);

            ParkingSlot ps9 = new ParkingSlot(null, "C-03", "ParkPoint Main Lot - Ground", 15.0, "AVAILABLE");
            slotRepository.save(ps9);

            // ParkPoint Plaza Lot
            ParkingSlot ps10 = new ParkingSlot(null, "P-01", "ParkPoint Plaza Lot - Level 1", 30.0, "AVAILABLE");
            slotRepository.save(ps10);

            ParkingSlot ps11 = new ParkingSlot(null, "P-02", "ParkPoint Plaza Lot - Level 1", 30.0, "AVAILABLE");
            ps11.setEvCharging(true);
            slotRepository.save(ps11);

            // ParkPoint Express Lot
            ParkingSlot ps12 = new ParkingSlot(null, "E-01", "ParkPoint Express Lot - Ground", 10.0, "AVAILABLE");
            ps12.setDisabilitySlot(true);
            slotRepository.save(ps12);

            ParkingSlot ps13 = new ParkingSlot(null, "E-02", "ParkPoint Express Lot - Ground", 10.0, "AVAILABLE");
            slotRepository.save(ps13);
        }

        if (userRepository.findByEmail("rkpooja425@gmail.com").isEmpty()) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("rkpooja425@gmail.com");
            admin.setPassword(passwordEncoder.encode("rkdev4215"));
            admin.setRole("ROLE_ADMIN");
            admin.setActive(true);
            userRepository.save(admin);
        }
    }
}