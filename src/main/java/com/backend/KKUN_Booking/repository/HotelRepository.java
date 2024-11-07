package com.backend.KKUN_Booking.repository;

import com.backend.KKUN_Booking.model.Hotel;
import com.backend.KKUN_Booking.model.User;
import com.backend.KKUN_Booking.model.enumModel.HotelCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, UUID> {
    // Các phương thức truy vấn tùy chỉnh (nếu cần)

    List<Hotel> findByCategory(HotelCategory category);
    List<Hotel> findByRatingGreaterThanEqual(double rating);
    boolean existsByOwner(User owner);

    @Query("SELECT h FROM Hotel h WHERE LOWER(h.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<Hotel> findByLocationContainingIgnoreCase(@Param("location") String location);

    // Native query to select top hotels ordered by rating, limited by the parameter
    // Method to find top hotels by rating
    @Query(value = "SELECT h.*, COUNT(b.id) AS booking_count " +
            "FROM hotels h " +
            "LEFT JOIN room r ON r.hotel_id = h.id " +
            "LEFT JOIN booking b ON b.room_id = r.id " +
            "GROUP BY h.id " +
            "ORDER BY h.rating DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Hotel> findTopHotelsByRating(@Param("limit") int limit);

    // Method to find trending destinations based on booking count
    @Query(value = "SELECT h.*, COUNT(b.id) AS booking_count " +
            "FROM hotels h " +
            "LEFT JOIN room r ON r.hotel_id = h.id " +
            "LEFT JOIN booking b ON b.room_id = r.id " +
            "GROUP BY h.id " +
            "ORDER BY booking_count DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Hotel> findTrendingDestinations(@Param("limit") int limit);
    Optional<Hotel> findByOwnerId(UUID ownerId);
}

