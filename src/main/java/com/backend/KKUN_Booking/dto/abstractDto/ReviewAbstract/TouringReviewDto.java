package com.backend.KKUN_Booking.dto.abstractDto.ReviewAbstract;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.backend.KKUN_Booking.dto.ReviewDto;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@JsonTypeName("touringReview")

public class TouringReviewDto extends ReviewDto {
    private UUID touringId;

    private int itinerary;
    private int attractions;
    private int tourGuide;
    private int food;

}
