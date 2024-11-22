package com.backend.KKUN_Booking.service.implement;

import com.backend.KKUN_Booking.dto.PromotionDto;
import com.backend.KKUN_Booking.model.Promotion;
import com.backend.KKUN_Booking.repository.PromotionRepository;
import com.backend.KKUN_Booking.service.PromotionService;
import com.backend.KKUN_Booking.util.VoucherUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PromotionServiceImpl implements PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    // Convert Entity to DTO
    private PromotionDto convertToDto(Promotion promotion) {
        PromotionDto dto = new PromotionDto();
        dto.setId(promotion.getId());
        dto.setName(promotion.getName());
        dto.setCode(promotion.getCode());
        dto.setStartDate(promotion.getStartDate());
        dto.setEndDate(promotion.getEndDate());
        dto.setQuantity(promotion.getQuantity());
        dto.setUsedCount(promotion.getUsedCount());
        dto.setValue(promotion.getValue());
        dto.setMaxDiscountValue(promotion.getMaxDiscountValue());
        dto.setDiscountType(promotion.getDiscountType());
        dto.setApplyTo(promotion.getApplyTo());
        dto.setDescription(promotion.getDescription());
        dto.setActive(promotion.isActive());
        return dto;
    }

    // Convert DTO to Entity
    private Promotion convertToEntity(PromotionDto dto) {
        Promotion promotion = new Promotion();
        promotion.setId(dto.getId());
        promotion.setName(dto.getName());
        promotion.setCode(dto.getCode());
        promotion.setStartDate(dto.getStartDate());
        promotion.setEndDate(dto.getEndDate());
        promotion.setQuantity(dto.getQuantity());
        promotion.setUsedCount(dto.getUsedCount());
        promotion.setValue(dto.getValue());
        promotion.setMaxDiscountValue(dto.getMaxDiscountValue());
        promotion.setDiscountType(dto.getDiscountType());
        promotion.setApplyTo(dto.getApplyTo());
        promotion.setDescription(dto.getDescription());
        promotion.setActive(dto.isActive());
        return promotion;
    }

    @Override
    public List<PromotionDto> getAllPromotions() {
        List<Promotion> promotions = promotionRepository.findAll();
        return promotions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PromotionDto> getPromotionById(UUID id) {
        Optional<Promotion> promotion = promotionRepository.findById(id);
        return promotion.map(this::convertToDto);
    }

    @Override
    public PromotionDto createPromotion(PromotionDto promotionDto) {
        if (promotionDto.getCode() == null || promotionDto.getCode().isEmpty()) {
            String generatedCode;
            do {
                generatedCode = VoucherUtil.generateRandomCode();
            } while (promotionRepository.existsByCode(generatedCode)); // Kiểm tra mã có tồn tại trong DB hay không
            promotionDto.setCode(generatedCode);
        }

        // Chuyển từ DTO sang entity và lưu vào DB
        Promotion promotion = convertToEntity(promotionDto);
        Promotion savedPromotion = promotionRepository.save(promotion);
        return convertToDto(savedPromotion);
    }

    @Override
    public PromotionDto updatePromotion(UUID id, PromotionDto promotionDto) {
        Optional<Promotion> existingPromotion = promotionRepository.findById(id);

        if (existingPromotion.isPresent()) {
            Promotion promotion = existingPromotion.get();
            promotion.setName(promotionDto.getName());
            promotion.setCode(promotionDto.getCode());
            promotion.setStartDate(promotionDto.getStartDate());
            promotion.setEndDate(promotionDto.getEndDate());
            promotion.setQuantity(promotionDto.getQuantity());
            promotion.setUsedCount(promotionDto.getUsedCount());
            promotion.setValue(promotionDto.getValue());
            promotion.setDiscountType(promotionDto.getDiscountType());
            promotion.setMaxDiscountValue(promotionDto.getMaxDiscountValue());
            promotion.setApplyTo(promotionDto.getApplyTo());
            promotion.setDescription(promotionDto.getDescription());
            promotion.setActive(promotionDto.isActive());
            Promotion updatedPromotion = promotionRepository.save(promotion);
            return convertToDto(updatedPromotion);
        } else {
            throw new RuntimeException("Promotion not found with ID: " + id);
        }
    }

    @Override
    public void deletePromotion(UUID id) {
        if (promotionRepository.existsById(id)) {
            promotionRepository.deleteById(id);
        } else {
            throw new RuntimeException("Promotion not found with ID: " + id);
        }
    }
    @Override
    public Optional<PromotionDto> getPromotionByCode(String code) {
        Optional<Promotion> promotion = promotionRepository.findByCode(code);
        return promotion.map(this::convertToDto);
    }

}
