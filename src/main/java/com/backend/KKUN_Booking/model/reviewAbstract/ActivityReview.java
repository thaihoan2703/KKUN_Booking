package com.backend.KKUN_Booking.model.reviewAbstract;

import com.backend.KKUN_Booking.model.Review;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class ActivityReview extends Review {
    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id")
    private Activity activity;*/

    private int funFactor;
    private int safety;
    private int equipmentQuality;
    @Override
    public double calculateOverallRating() {
        return (funFactor + safety + equipmentQuality) / 3.0;
    }
}

