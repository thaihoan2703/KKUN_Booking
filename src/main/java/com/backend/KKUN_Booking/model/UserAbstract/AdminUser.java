package com.backend.KKUN_Booking.model.UserAbstract;

import com.backend.KKUN_Booking.model.User;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter

public class AdminUser extends User {
    @ElementCollection
    private List<String> managedSections;

    private int actionCount;
}
