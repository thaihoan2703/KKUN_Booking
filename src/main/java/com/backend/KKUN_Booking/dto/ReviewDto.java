package com.backend.KKUN_Booking.dto;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.backend.KKUN_Booking.dto.abstractDto.ReviewAbstract.RoomReviewDto;
import com.backend.KKUN_Booking.dto.abstractDto.ReviewAbstract.TouringReviewDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RoomReviewDto.class, name = "roomReview"),
        @JsonSubTypes.Type(value = TouringReviewDto.class, name = "touringReview")
})
public abstract class ReviewDto {
    private UUID id;
    private String comment;
    private LocalDateTime date;
    private double overallRating;
    private UserDto user;
}

