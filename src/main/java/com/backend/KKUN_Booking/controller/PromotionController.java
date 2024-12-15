package com.backend.KKUN_Booking.controller;


import com.backend.KKUN_Booking.dto.PromotionDto;
import com.backend.KKUN_Booking.model.enumModel.DiscountType;
import com.backend.KKUN_Booking.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {
    @Autowired
    private PromotionService promotionService;

    @GetMapping
    public ResponseEntity<List<PromotionDto>> getAllPromotions() {
        return ResponseEntity.ok(promotionService.getAllPromotions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionDto> getPromotionById(@PathVariable UUID id) {
        return promotionService.getPromotionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PromotionDto> createPromotion(@RequestBody PromotionDto promotionDto) {
        return ResponseEntity.ok(promotionService.createPromotion(promotionDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromotionDto> updatePromotion(@PathVariable UUID id, @RequestBody PromotionDto promotionDto) {
        return ResponseEntity.ok(promotionService.updatePromotion(id, promotionDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePromotion(@PathVariable UUID id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/code/{voucherCode}")
    public ResponseEntity<PromotionDto> getPromotionByCode(@PathVariable String voucherCode) {
        return promotionService.getPromotionByCode(voucherCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/discount-types")
    public ResponseEntity<List<Map<String, String>>> getDiscountTypes() {
        List<Map<String, String>> discountTypes = Arrays.stream(DiscountType.values())
                .map(type -> Map.of(
                        "value", type.name(),
                        "label", type.getDisplayName()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(discountTypes);
    }
}
