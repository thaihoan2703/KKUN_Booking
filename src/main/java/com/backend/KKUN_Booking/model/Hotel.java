package com.backend.KKUN_Booking.model;

import com.backend.KKUN_Booking.model.enumModel.PaymentPolicy;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "hotels")  // Đổi tên bảng thành "hotels"
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;



    private String name;
    private String category;
    private Double rating;
    private String location;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User owner;
    @ElementCollection
    private List<String> exteriorImages;

    @ElementCollection
    private List<String> roomImages;


    private PaymentPolicy paymentPolicy; // "ONLINE" or "CHECKOUT"


    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Room> rooms = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "hotel_amenity",
            joinColumns = @JoinColumn(name = "hotel_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private List<Amenity> amenities = new ArrayList<>();


    // Getters and Setters
}

