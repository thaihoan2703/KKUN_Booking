package com.backend.KKUN_Booking.service;


import com.backend.KKUN_Booking.dto.PromotionDto;
import com.backend.KKUN_Booking.model.Promotion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromotionService {
    List<PromotionDto> getAllPromotions();
    Optional<PromotionDto> getPromotionById(UUID id);
    PromotionDto createPromotion(PromotionDto promotionDto);
    PromotionDto updatePromotion(UUID id, PromotionDto promotionDto);
    void deletePromotion(UUID id);
}