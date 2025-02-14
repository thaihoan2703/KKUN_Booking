package com.backend.KKUN_Booking.service;

import com.backend.KKUN_Booking.dto.UserDto;
import com.backend.KKUN_Booking.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<UserDto> getAllUsers();
    UserDto getUserById(UUID id);
    UserDto getUserByEmail(String email);
    UserDto createUser(UserDto userDto);
    UserDto updateUser(UUID id, UserDto userDto, MultipartFile profileImage);
    void deleteUser(UUID id);
    void changePassword( UUID userId, String oldPassword, String newPassword);
    User findOrSaveOauthUser(String email, String name);
    UserDetails loadUserByUsername(String username);
    void addRecentSearch(UUID userId, String searchTerm);
    void saveSaveHotel(UUID userId, UUID hotelId);

    void removeSavedHotel(UUID userId, UUID hotelId);
    List<String> getRecentSearches(UUID userId);
    List<String> getSavedHotels(UUID userId);
}
