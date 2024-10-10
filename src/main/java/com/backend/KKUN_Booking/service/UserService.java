package com.backend.KKUN_Booking.service;

import com.backend.KKUN_Booking.dto.UserDto;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<UserDto> getAllUsers();
    UserDto getUserById(UUID id);
    UserDto getUserByEmail(String email);
    UserDto createUser(UserDto userDto);
    UserDto updateUser(UUID id, UserDto userDto);
    void deleteUser(UUID id);

    void saveOauthUser(String email);

    void addRecentSearch(UUID userId, String searchTerm);
    void saveSaveHotel(UUID userId, UUID hotelId);

    void removeSavedHotel(UUID userId, UUID hotelId);
    List<String> getRecentSearches(UUID userId);
    List<String> getSavedHotels(UUID userId);
}
