package com.backend.KKUN_Booking.controller;

import com.backend.KKUN_Booking.dto.BookingDto;
import com.backend.KKUN_Booking.dto.ChangePasswordRequest;
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
import com.backend.KKUN_Booking.security.UserDetailsImpl;
import com.backend.KKUN_Booking.service.BookingService;
import com.backend.KKUN_Booking.service.PaymentService;
import com.backend.KKUN_Booking.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private  final UserRepository userRepository;
    private final BookingService bookingService;
    public UserController(UserService userService, UserRepository userRepository, BookingService bookingService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users); // Trả về danh sách người dùng với trạng thái 200 OK
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable UUID id, Principal principal) {
        // Lấy email của người dùng hiện tại từ Principal
        String currentUserEmail = principal.getName();

        // Kiểm tra xem người dùng có quyền truy cập thông tin người dùng khác không
        UserDto user = userService.getUserById(id);
        if (!user.getEmail().equals(currentUserEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền truy cập thông tin của người dùng này.");
        }

        return ResponseEntity.ok(user); // Trả về thông tin người dùng với trạng thái 200 OK
    }

    // Endpoint đổi mật khẩu cho người dùng hiện tại
    @PostMapping("/me/change-password")
    public ResponseEntity<String> changePassword(
            @RequestBody ChangePasswordRequest request,
            Principal principal) {
        try {
            // Lấy email của người dùng hiện tại từ Principal
            String currentUserEmail = principal.getName();
            User currentUser = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Kiểm tra nếu newPassword và confirmNewPassword có khớp không
            if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
                return ResponseEntity.badRequest().body("Mật khẩu mới và xác nhận mật khẩu không khớp");
            }

            // Thay đổi mật khẩu cho người dùng hiện tại
            userService.changePassword(currentUser.getId(), request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok("Mật khẩu đã được thay đổi thành công.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mật khẩu cũ không chính xác.");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi khi thay đổi mật khẩu.");
        }
    }

    // Endpoint cập nhật thông tin người dùng hiện tại
    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDto> updateCurrentUser(
            @RequestPart("user") String userDtoJson,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            Principal principal) {

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule());

        try {
            // Xác định người dùng hiện tại từ email trong Principal
            String currentUserEmail = principal.getName();
            User currentUser = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Parse JSON thành UserDto
            UserDto userDto = mapper.readValue(userDtoJson, UserDto.class);

            // Kiểm tra và xử lý update, đảm bảo không thay đổi loại người dùng
            if (!isValidUserTypeForUpdate(currentUser, userDto)) {
                throw new IllegalArgumentException("Cannot change user type during update");
            }

            return ResponseEntity.ok(userService.updateUser(currentUser.getId(), userDto, profileImage));

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

    @GetMapping("/booking-hotel/history")
    public ResponseEntity<List<BookingDto>> getBookingHistory(Principal principal) {
        String userEmail = principal.getName();
        List<BookingDto> bookingDtos = bookingService.getBookingHistory(userEmail);
        return ResponseEntity.ok(bookingDtos);
    }


}
