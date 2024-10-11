package com.backend.KKUN_Booking.model;

import com.backend.KKUN_Booking.converter.StringListConverter;
import com.backend.KKUN_Booking.model.reviewAbstract.RoomReview;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "room")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String type;
    private Integer capacity;
    private Double basePrice;
    private Boolean available;

    @Convert(converter = StringListConverter.class)
    @Column(name = "room_images", columnDefinition = "TEXT") // Lưu dưới dạng TEXT
    private List<String> roomImages = new ArrayList<>(); // Khởi tạo danh sách

    @ManyToMany
    @JoinTable(
            name = "room_amenity",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private List<Amenity> amenities = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @OneToMany(mappedBy = "room")
    private List<Booking> bookings;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomReview> reviews = new ArrayList<>();

    public void addReview(RoomReview review) {
        reviews.add(review);
        review.setRoom(this);
    }

    public double getAverageRating() {
        if (reviews.isEmpty()) return 0;
        return reviews.stream().mapToDouble(Review::calculateOverallRating).average().orElse(0);
    }
    // Getters and Setters

}
