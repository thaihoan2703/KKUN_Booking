package com.backend.KKUN_Booking.service.implement;

import com.backend.KKUN_Booking.dto.AmenityDto;
import com.backend.KKUN_Booking.exception.ResourceNotFoundException;
import com.backend.KKUN_Booking.model.Amenity;
import com.backend.KKUN_Booking.repository.AmenityRepository;
import com.backend.KKUN_Booking.service.AmenityService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AmenityServiceImpl implements AmenityService {

    private final AmenityRepository amenityRepository;

    public AmenityServiceImpl(AmenityRepository amenityRepository) {
        this.amenityRepository = amenityRepository;
    }

    @Override
    public List<AmenityDto> getAllAmenities() {
        return amenityRepository.findAll().stream()
                .map(this::convertToDto)
                .sorted((a1, a2) -> a1.getAmenityType().compareTo(a2.getAmenityType()))
                .collect(Collectors.toList());
    }
    @Override
    public List<String> getAmenitiesByIds(List<UUID> amenityIds) {
        return amenityRepository.findAllById(amenityIds)
                .stream()
                .map(Amenity::getName) // Assuming Amenity class has a getName method
                .collect(Collectors.toList());
    }
    @Override
    public AmenityDto getAmenityById(UUID id) {
        return amenityRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity not found"));
    }

    @Override
    public AmenityDto createAmenity(AmenityDto amenityDto) {
        // Kiểm tra xem tên tiện ích đã tồn tại chưa
        Optional<Amenity> existingAmenity = amenityRepository.findByNameAndAmenityType(amenityDto.getName(), amenityDto.getAmenityType());

        if (existingAmenity.isPresent()) {
            throw new IllegalArgumentException("Tên tiện ích đã tồn tại. Vui lòng chọn tên khác.");
        }

        // Nếu không trùng tên, tiến hành tạo mới tiện ích
        Amenity amenity = convertToEntity(amenityDto);
        return convertToDto(amenityRepository.save(amenity));
    }

    @Override
    public AmenityDto updateAmenity(UUID id, AmenityDto amenityDto) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity not found"));
        amenity.setName(amenityDto.getName());
        amenity.setDescription(amenityDto.getDescription());
        return convertToDto(amenityRepository.save(amenity));
    }

    @Override
    public void deleteAmenity(UUID id) {
        amenityRepository.deleteById(id);
    }

    private AmenityDto convertToDto(Amenity amenity) {
        AmenityDto amenityDto = new AmenityDto();
        amenityDto.setId(amenity.getId());
        amenityDto.setName(amenity.getName());
        amenityDto.setDescription(amenity.getDescription());
        amenityDto.setAmenityType(amenity.getAmenityType());
        return amenityDto;
    }


    private Amenity convertToEntity(AmenityDto amenityDto) {
        Amenity amenity = new Amenity();
        amenity.setId(amenityDto.getId());
        amenity.setName(amenityDto.getName());
        amenity.setDescription(amenityDto.getDescription());
        amenity.setAmenityType(amenityDto.getAmenityType());

        return amenity;
    }

}

