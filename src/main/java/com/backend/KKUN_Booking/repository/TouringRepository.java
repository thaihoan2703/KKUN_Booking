package com.backend.KKUN_Booking.repository;


import com.backend.KKUN_Booking.model.Touring;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TouringRepository extends JpaRepository<Touring, UUID> {

}
