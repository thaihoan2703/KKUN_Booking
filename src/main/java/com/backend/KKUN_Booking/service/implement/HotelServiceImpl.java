package com.backend.KKUN_Booking.service.implement;

import com.backend.KKUN_Booking.dto.HotelDto;
import com.backend.KKUN_Booking.dto.RoomDto;
import com.backend.KKUN_Booking.exception.ResourceNotFoundException;
import com.backend.KKUN_Booking.model.*;
import com.backend.KKUN_Booking.model.enumModel.PaymentPolicy;
import com.backend.KKUN_Booking.model.enumModel.RoleUser;
import com.backend.KKUN_Booking.repository.AmenityRepository;
import com.backend.KKUN_Booking.repository.HotelRepository;
import com.backend.KKUN_Booking.repository.UserRepository;
import com.backend.KKUN_Booking.service.HotelService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final AmenityRepository amenityRepository;

    public HotelServiceImpl(HotelRepository hotelRepository, UserRepository userRepository, AmenityRepository amenityRepository) {
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
        this.amenityRepository = amenityRepository;
    }

    @Override
    public List<HotelDto> getAllHotels() {
        return hotelRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public HotelDto getHotelById(UUID id) {
        return hotelRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));
    }

    @Override
    public HotelDto createHotel(HotelDto hotelDto, String userEmail) {
        // Find user by email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user is a hotel owner
        if (!user.getRole().getName().equals(RoleUser.HOTELOWNER.name())) {
            throw new IllegalArgumentException("User is not a hotel owner");
        }

        // Check if user already has a hotel
        if (hotelRepository.existsByOwner(user)) {
            throw new IllegalArgumentException("User already has a hotel and can only create one.");
        }

        // Validate payment policy
        if (hotelDto.getPaymentPolicy() == null ||
                (!hotelDto.getPaymentPolicy().equals(PaymentPolicy.ONLINE) &&
                        !hotelDto.getPaymentPolicy().equals(PaymentPolicy.CHECKOUT))) {
            throw new IllegalArgumentException("Invalid payment policy. It must be either 'ONLINE' or 'CHECKOUT'.");
        }

        // Create new hotel and link it to the user
        Hotel hotel = convertToEntity(hotelDto);
        hotel.setOwner(user);  // Link hotel with user
        return convertToDto(hotelRepository.save(hotel));
    }

    @Override
    public HotelDto updateHotel(UUID id, HotelDto hotelDto, String userEmail) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        // Find user by email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if the user is not the hotel owner
        if (!hotel.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You do not have permission to update this hotel.");
        }

        // Validate payment policy
        if (hotelDto.getPaymentPolicy() == null ||
                (!hotelDto.getPaymentPolicy().equals(PaymentPolicy.ONLINE.name()) &&
                        !hotelDto.getPaymentPolicy().equals(PaymentPolicy.CHECKOUT.name()))) {
            throw new IllegalArgumentException("Invalid payment policy. It must be either 'ONLINE' or 'CHECKOUT'.");
        }

        // Update hotel information
        hotel.setName(hotelDto.getName());
        hotel.setCategory(hotelDto.getCategory());
        hotel.setRating(hotelDto.getRating());
        hotel.setLocation(hotelDto.getLocation());
        hotel.setExteriorImages(hotelDto.getExteriorImages());
        hotel.setRoomImages(hotelDto.getRoomImages());
        hotel.setPaymentPolicy(hotelDto.getPaymentPolicy());  // Update payment policy

        return convertToDto(hotelRepository.save(hotel));
    }

    @Override
    public void deleteHotel(UUID id) {
        hotelRepository.deleteById(id);
    }

    @Override
    public List<HotelDto> findTopHotelsByRating(int limit) {
        // Fetch top hotels by rating
        List<Hotel> hotels = hotelRepository.findTopHotelsByRating(limit);

        // Map to HotelDto and return
        return hotels.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<HotelDto> findTrendingDestinations(int limit) {
        // Fetch trending destinations
        List<Hotel> hotels = hotelRepository.findTrendingDestinations(limit);

        // Map to HotelDto and return
        return hotels.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private HotelDto convertToDto(Hotel hotel) {
        HotelDto hotelDto = new HotelDto();
        hotelDto.setId(hotel.getId());
        hotelDto.setName(hotel.getName());
        hotelDto.setCategory(hotel.getCategory());
        hotelDto.setRating(hotel.getRating());
        hotelDto.setLocation(hotel.getLocation());
        hotelDto.setExteriorImages(hotel.getExteriorImages());
        hotelDto.setRoomImages(hotel.getRoomImages());
        hotelDto.setPaymentPolicy(hotel.getPaymentPolicy());

        // Convert amenities to list of IDs
        List<UUID> amenityIds = hotel.getAmenities().stream()
                .map(Amenity::getId)
                .collect(Collectors.toList());
        hotelDto.setAmenityIds(amenityIds);

        // Convert rooms from entity to DTO
        List<RoomDto> roomDtos = hotel.getRooms().stream()
                .map(this::convertRoomToDto)
                .collect(Collectors.toList());
        hotelDto.setRooms(roomDtos);

        return hotelDto;
    }

    private Hotel convertToEntity(HotelDto hotelDto) {
        Hotel hotel = new Hotel();
        hotel.setId(hotelDto.getId());
        hotel.setName(hotelDto.getName());
        hotel.setCategory(hotelDto.getCategory());
        hotel.setRating(hotelDto.getRating());
        hotel.setLocation(hotelDto.getLocation());
        hotel.setExteriorImages(hotelDto.getExteriorImages());
        hotel.setRoomImages(hotelDto.getRoomImages());
        hotel.setPaymentPolicy(hotelDto.getPaymentPolicy());

        // Convert rooms from DTO to entity and set relationship
        if (hotelDto.getRooms() != null) {
            List<Room> rooms = hotelDto.getRooms().stream()
                    .map(roomDto -> {
                        Room room = convertRoomToEntity(roomDto);
                        room.setHotel(hotel);  // Set hotel for the room
                        return room;
                    })
                    .collect(Collectors.toList());
            hotel.setRooms(rooms);
        } else {
            hotel.setRooms(new ArrayList<>());
        }

        // Convert amenities from DTO to entity and set relationship
        if (hotelDto.getAmenityIds() != null) {
            List<Amenity> amenities = hotelDto.getAmenityIds().stream()
                    .map(amenityId -> {
                        Amenity amenity = new Amenity(); // Or fetch from database if needed
                        amenity.setId(amenityId); // Set amenity ID
                        return amenity;
                    })
                    .collect(Collectors.toList());
            hotel.setAmenities(amenities); // Assuming you have a setAmenities method in Hotel
        }

        return hotel;
    }

    private RoomDto convertRoomToDto(Room room) {
        RoomDto roomDto = new RoomDto();
        roomDto.setId(room.getId());
        roomDto.setType(room.getType());
        roomDto.setCapacity(room.getCapacity());
        roomDto.setBasePrice(room.getBasePrice());
        roomDto.setAvailable(room.getAvailable());
        return roomDto;
    }

    private Room convertRoomToEntity(RoomDto roomDto) {
        Room room = new Room();
        room.setId(roomDto.getId());  // Ensure UUID is handled correctly
        room.setType(roomDto.getType());
        room.setCapacity(roomDto.getCapacity());
        room.setBasePrice(roomDto.getBasePrice());
        room.setAvailable(roomDto.isAvailable());
        return room;
    }


}
