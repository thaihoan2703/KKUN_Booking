package com.backend.KKUN_Booking.controller;

import com.backend.KKUN_Booking.dto.UserDto;
import com.backend.KKUN_Booking.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users); // Trả về danh sách người dùng với trạng thái 200 OK
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user); // Trả về người dùng với trạng thái 200 OK
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID id, @RequestBody UserDto userDto) {
        UserDto updatedUser = userService.updateUser(id, userDto);
        return ResponseEntity.ok(updatedUser); // Trả về người dùng đã cập nhật với trạng thái 200 OK
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build(); // Trả về 204 No Content khi xóa thành công
    }
}
