package com.backend.KKUN_Booking.controller;

import com.backend.KKUN_Booking.dto.HotelDto;
import com.backend.KKUN_Booking.dto.UserDto;
import com.backend.KKUN_Booking.dto.abstractDto.UserAbstract.AdminUserDto;
import com.backend.KKUN_Booking.dto.abstractDto.UserAbstract.CustomerUserDto;
import com.backend.KKUN_Booking.dto.abstractDto.UserAbstract.HotelOwnerUserDto;
import com.backend.KKUN_Booking.exception.ResourceNotFoundException;
import com.backend.KKUN_Booking.model.User;
import com.backend.KKUN_Booking.model.UserAbstract.AdminUser;
import com.backend.KKUN_Booking.model.UserAbstract.CustomerUser;
import com.backend.KKUN_Booking.model.UserAbstract.HotelOwnerUser;
import com.backend.KKUN_Booking.repository.UserRepository;
import com.backend.KKUN_Booking.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private  final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
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

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDto> updateUser(
            @PathVariable UUID id,
            @RequestPart("user") String userDtoJson,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule());

        try {
            // Đọc JSON để xác định type
            JsonNode jsonNode = mapper.readTree(userDtoJson);
            String type = jsonNode.has("type") ? jsonNode.get("type").asText() : null;

            // Nếu không có type, thử lấy từ database
            if (type == null) {
                User existingUser = userRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                type = determineUserType(existingUser);
            }

            // Thêm type vào JSON nếu chưa có
            if (type != null && !jsonNode.has("type")) {
                ((ObjectNode) jsonNode).put("type", type);
                userDtoJson = mapper.writeValueAsString(jsonNode);
            }

            // Parse JSON thành UserDto
            UserDto userDto = mapper.readValue(userDtoJson, UserDto.class);

            // Kiểm tra và xử lý update
            User existingUser = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            if (!isValidUserTypeForUpdate(existingUser, userDto)) {
                throw new IllegalArgumentException("Cannot change user type during update");
            }

            return ResponseEntity.ok(userService.updateUser(id, userDto, profileImage));

        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid user data format", e);
        }
    }

    private String determineUserType(User user) {
        if (user instanceof AdminUser) return "admin";
        if (user instanceof CustomerUser) return "customer";
        if (user instanceof HotelOwnerUser) return "hotelowner";
        return null;
    }
    private boolean isValidUserTypeForUpdate(User user, UserDto dto) {
        return (user instanceof AdminUser && dto instanceof AdminUserDto) ||
                (user instanceof CustomerUser && dto instanceof CustomerUserDto) ||
                (user instanceof HotelOwnerUser && dto instanceof HotelOwnerUserDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build(); // Trả về 204 No Content khi xóa thành công
    }
}
