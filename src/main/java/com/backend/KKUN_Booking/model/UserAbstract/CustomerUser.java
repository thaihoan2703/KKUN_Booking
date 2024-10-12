package com.backend.KKUN_Booking.model.UserAbstract;

import com.backend.KKUN_Booking.model.Booking;
import com.backend.KKUN_Booking.model.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class CustomerUser extends User {
    // Additional fields specific to customers, if any
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();
}