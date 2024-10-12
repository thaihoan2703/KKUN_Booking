package com.backend.KKUN_Booking.model.UserAbstract;

import com.backend.KKUN_Booking.model.Hotel;
import com.backend.KKUN_Booking.model.User;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class HotelOwnerUser extends User {
    @OneToOne(mappedBy = "owner")
    private Hotel hotel;

}