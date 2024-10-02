package com.backend.KKUN_Booking.dto.abstractDto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.backend.KKUN_Booking.dto.ReviewDto;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@JsonTypeName("roomReview")
public class RoomReviewDto extends ReviewDto {
    private UUID roomId;
    private int cleanliness;
    private int amenities;
    private int space;
    private int comfort;
}
