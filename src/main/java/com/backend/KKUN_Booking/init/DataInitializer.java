package com.backend.KKUN_Booking.init;

import com.backend.KKUN_Booking.model.Role;
import com.backend.KKUN_Booking.model.enumModel.RoleUser;
import com.backend.KKUN_Booking.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) { // Check if roles already exist
            Role adminRole = new Role();
            adminRole.setName(RoleUser.ADMIN.name());
            adminRole.setDescription("Administrator role with full access.");

            Role userRole = new Role();
            userRole.setName(RoleUser.CUSTOMER.name());
            userRole.setDescription("Regular customer role.");

            Role hotelOwnerRole = new Role();
            hotelOwnerRole.setName(RoleUser.HOTELOWNER.name());
            hotelOwnerRole.setDescription("Role for hotel owners.");

            roleRepository.saveAll(Arrays.asList(adminRole, userRole, hotelOwnerRole));
        }
    }
}

