package com.backend.KKUN_Booking.repository;

import com.backend.KKUN_Booking.model.Amenity;
import com.backend.KKUN_Booking.model.enumModel.AmenityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, UUID> {
    // Các phương thức truy vấn tùy chỉnh (nếu cần)
    List<Amenity> findAllById(Iterable<UUID> ids);
    Optional<Amenity> findByName(String name);
    Optional<Amenity> findByNameAndAmenityType(String name, AmenityType amenityType);
}

