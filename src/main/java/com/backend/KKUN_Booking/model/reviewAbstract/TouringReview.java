package com.backend.KKUN_Booking.model.reviewAbstract;

import com.backend.KKUN_Booking.model.Review;
import com.backend.KKUN_Booking.model.Touring;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class TouringReview extends Review {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "touring_id")
    private Touring touring;

    private int itinerary;
    private int attractions;
    private int tourGuide;
    private int food;

    @Override
    public double calculateOverallRating() {
        return (itinerary + attractions + tourGuide + food) / 4.0;
    }
}

