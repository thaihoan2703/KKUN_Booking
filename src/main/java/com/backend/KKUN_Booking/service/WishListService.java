package com.backend.KKUN_Booking.service;

import com.backend.KKUN_Booking.dto.WishListDto;

import java.util.List;
import java.util.UUID;

public interface WishListService {
    // Thêm mới mục vào danh sách yêu thích
    WishListDto addToWishList(WishListDto wishListDto, String userEmail);
    // Lấy tất cả các mục trong danh sách yêu thích của người dùng
    List<WishListDto> getAllWishesByUserId(UUID userId);
    // Lấy mục trong danh sách yêu thích theo ID
    WishListDto getWishListById(UUID id);
    // Cập nhật mục trong danh sách yêu thích
    WishListDto updateWishList(UUID id, WishListDto wishListDto);
    // Xóa mục trong danh sách yêu thích
    void removeFromWishList(UUID id);
}
