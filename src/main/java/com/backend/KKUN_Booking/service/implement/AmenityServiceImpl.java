package com.backend.KKUN_Booking.service.implement;

import com.backend.KKUN_Booking.dto.AmenityDto;
import com.backend.KKUN_Booking.exception.ResourceNotFoundException;
import com.backend.KKUN_Booking.model.Amenity;
import com.backend.KKUN_Booking.repository.AmenityRepository;
import com.backend.KKUN_Booking.service.AmenityService;
import org.springframework.stereotype.Service;

import java.util.List;
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
        return amenityDto;
    }


    private Amenity convertToEntity(AmenityDto amenityDto) {
        Amenity amenity = new Amenity();
        amenity.setId(amenityDto.getId());
        amenity.setName(amenityDto.getName());
        amenity.setDescription(amenityDto.getDescription());
        return amenity;
    }

}

