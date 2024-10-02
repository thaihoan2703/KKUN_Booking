package com.backend.KKUN_Booking.repository;

import com.backend.KKUN_Booking.model.WishList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WishListRepository extends JpaRepository<WishList, UUID> {
    List<WishList> findByUserId(UUID userId);
}
