package com.backend.KKUN_Booking.model;

import com.backend.KKUN_Booking.converter.StringListConverter;
import com.backend.KKUN_Booking.model.enumModel.HotelCategory;
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

    @Enumerated(EnumType.STRING)
    private HotelCategory category;
    private Double rating;
    private String location;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User owner;

    @Convert(converter = StringListConverter.class)
    @Column(name = "exterior_images", columnDefinition = "TEXT") // Lưu dưới dạng TEXT
    private List<String> exteriorImages = new ArrayList<>(); // Khởi tạo danh sách

    @Convert(converter = StringListConverter.class)
    @Column(name = "room_images", columnDefinition = "TEXT") // Lưu dưới dạng TEXT
    private List<String> roomImages = new ArrayList<>(); // Khởi tạo danh sách


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

    @Column(name = "num_of_reviews", nullable = false, columnDefinition = "integer default 0")
    private int numOfReviews = 0;
    // Method to update the hotel's average rating based on its rooms
    public void updateRating() {
        if (rooms.isEmpty()) {
            this.rating = 0.0;  // Default to 0 if there are no rooms
            return;
        }

        // Collect all valid reviews from all rooms
        List<Double> allRatings = rooms.stream()
                .flatMap(room -> room.getReviews().stream())  // Get all reviews from each room
                .map(Review::getOverallRating)  // Extract the rating from each review
                .filter(rating -> rating != null)  // Filter out null ratings
                .toList();  // Collect into a list

        // If there are no valid ratings, set the rating to 0
        if (allRatings.isEmpty()) {
            this.rating = 0.0;
            return;
        }

        // Calculate the average rating based on all review ratings
        double averageRating = allRatings.stream()
                .mapToDouble(Double::doubleValue)  // Convert to double stream
                .average()  // Calculate the average rating
                .orElse(0.0);  // Default to 0 if no ratings exist

        this.rating = averageRating;  // Update the hotel's rating to the calculated average
    }
    public void updateNumOfReviews() {
        // Đếm tổng số review của tất cả các phòng thuộc khách sạn này
        int totalReviews = rooms.stream()
                .mapToInt(room -> room.getReviews().size())  // Lấy số lượng review của mỗi phòng
                .sum();  // Tính tổng số review từ tất cả các phòng

        this.numOfReviews = totalReviews;  // Cập nhật giá trị cho numOfReviews
    }
    // Getters and Setters
}

