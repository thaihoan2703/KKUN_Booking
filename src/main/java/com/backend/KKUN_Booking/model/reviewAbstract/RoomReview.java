package com.backend.KKUN_Booking.model.reviewAbstract;

import com.backend.KKUN_Booking.model.Review;
import com.backend.KKUN_Booking.model.Room;
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
public class RoomReview extends Review {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    private int cleanliness;
    private int amenities;
    private int space;
    private int comfort;

    @Override
    public double calculateOverallRating() {
        return (cleanliness + amenities + space + comfort) / 4.0;
    }

    public void updateRatings(int cleanliness, int amenities, int space, int comfort) {
        this.cleanliness = cleanliness;
        this.amenities = amenities;
        this.space = space;
        this.comfort = comfort;
        updateOverallRating();
    }
}
