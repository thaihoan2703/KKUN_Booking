package com.backend.KKUN_Booking.model;

import com.backend.KKUN_Booking.model.reviewAbstract.TouringReview;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Touring {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToMany(mappedBy = "touring", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TouringReview> reviews = new ArrayList<>();


}
