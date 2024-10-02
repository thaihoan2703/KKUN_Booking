package com.backend.KKUN_Booking.service.implement;

import com.backend.KKUN_Booking.dto.RoomDto;
import com.backend.KKUN_Booking.dto.WishListDto;
import com.backend.KKUN_Booking.exception.ResourceNotFoundException;
import com.backend.KKUN_Booking.model.Room;
import com.backend.KKUN_Booking.model.User;
import com.backend.KKUN_Booking.model.WishList;
import com.backend.KKUN_Booking.repository.UserRepository;
import com.backend.KKUN_Booking.repository.WishListRepository;
import com.backend.KKUN_Booking.service.RoomService;
import com.backend.KKUN_Booking.service.UserService;
import com.backend.KKUN_Booking.service.WishListService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class WishListServiceImpl implements WishListService {

    private final WishListRepository wishListRepository;
    private final UserRepository userRepository;
    private final UserService userService; // Assuming you have a UserService
    private final RoomService roomService; // Assuming you have a RoomService

    public WishListServiceImpl(WishListRepository wishListRepository, UserRepository userRepository, UserService userService, RoomService roomService) {
        this.wishListRepository = wishListRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.roomService = roomService;
    }

    // Thêm mới mục vào danh sách yêu thích
    @Override
    public WishListDto addToWishList(WishListDto wishListDto, String userEmail ) {
        // Tìm người dùng theo email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        WishList wishList = convertToEntity(wishListDto);
        wishList.setUser(user);
        wishListRepository.save(wishList);
        return convertToDto(wishList);
    }

    // Lấy tất cả các mục trong danh sách yêu thích
    @Override
    public List<WishListDto> getAllWishesByUserId(UUID userId) {
        return wishListRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Lấy mục trong danh sách yêu thích theo ID
    public WishListDto getWishListById(UUID id) {
        WishList wishList = wishListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WishList not found with ID: " + id));
        return convertToDto(wishList);
    }

    // Cập nhật mục trong danh sách yêu thích
    @Override
    public WishListDto updateWishList(UUID id, WishListDto wishListDto) {


        return null;
    }

    // Xóa mục trong danh sách yêu thích
    public void removeFromWishList(UUID id) {
        WishList wishList = wishListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WishList not found with ID: " + id));
        wishListRepository.delete(wishList);
    }

    // Chuyển đổi WishList sang WishListDto
    private WishListDto convertToDto(WishList wishList) {
        WishListDto dto = new WishListDto();
        dto.setId(wishList.getId());
        dto.setUserId(wishList.getUser().getId());
        dto.setRoomId(wishList.getRoom().getId());
        return dto;
    }

    // Chuyển đổi từ WishListDto sang WishList entity
    private WishList convertToEntity(WishListDto wishListDto) {
        WishList wishList = new WishList();
        wishList.setId(wishListDto.getId());
        // Set the room using the roomId from the WishListDto
        if (wishListDto.getRoomId() != null) {

            Room room = convertRoomToEntity(roomService.getRoomById(wishListDto.getRoomId()));
            wishList.setRoom(room);
        }
        return wishList;
    }

    private Room convertRoomToEntity(RoomDto roomDto) {
        Room room = new Room();
        room.setId(roomDto.getId());  // Đảm bảo rằng UUID được xử lý đúng
        room.setType(roomDto.getType());
        room.setCapacity(roomDto.getCapacity());
        room.setBasePrice(roomDto.getBasePrice());
        room.setAvailable(roomDto.isAvailable());
        return room;
    }

}
