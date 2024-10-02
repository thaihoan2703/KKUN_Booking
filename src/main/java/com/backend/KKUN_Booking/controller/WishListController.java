package com.backend.KKUN_Booking.controller;

import com.backend.KKUN_Booking.dto.WishListDto;
import com.backend.KKUN_Booking.service.WishListService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wishlist")
public class WishListController {

    private final WishListService wishListService;

    public WishListController(WishListService wishListService) {
        this.wishListService = wishListService;
    }

    // Thêm mới mục vào danh sách yêu thích
    @PostMapping
    public ResponseEntity<WishListDto> addToWishList(@RequestBody WishListDto wishListDto,  Principal principal) {
        // Lấy email hoặc username
        String userEmail = principal.getName();  // Lấy email hoặc username từ authentication

        WishListDto createdWishList = wishListService.addToWishList(wishListDto,userEmail);
        return ResponseEntity.status(201).body(createdWishList); // 201 Created
    }

    // Lấy tất cả các mục trong danh sách yêu thích của người dùng
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WishListDto>> getAllWishesByUserId(@PathVariable UUID userId) {
        List<WishListDto> wishListDtos = wishListService.getAllWishesByUserId(userId);
        return ResponseEntity.ok(wishListDtos); // 200 OK
    }

    // Lấy mục trong danh sách yêu thích theo ID
    @GetMapping("/{id}")
    public ResponseEntity<WishListDto> getWishListById(@PathVariable UUID id) {
        WishListDto wishListDto = wishListService.getWishListById(id);
        return ResponseEntity.ok(wishListDto); // 200 OK
    }

    // Cập nhật mục trong danh sách yêu thích
    @PutMapping("/{id}")
    public ResponseEntity<WishListDto> updateWishList(@PathVariable UUID id, @RequestBody WishListDto wishListDto) {
        WishListDto updatedWishList = wishListService.updateWishList(id, wishListDto);
        return ResponseEntity.ok(updatedWishList); // 200 OK
    }

    // Xóa mục trong danh sách yêu thích
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeFromWishList(@PathVariable UUID id) {
        wishListService.removeFromWishList(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
