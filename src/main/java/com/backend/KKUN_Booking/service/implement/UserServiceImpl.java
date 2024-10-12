package com.backend.KKUN_Booking.service.implement;

import com.backend.KKUN_Booking.dto.UserDto;
import com.backend.KKUN_Booking.dto.abstractDto.UserAbstract.AdminUserDto;
import com.backend.KKUN_Booking.dto.abstractDto.UserAbstract.CustomerUserDto;
import com.backend.KKUN_Booking.dto.abstractDto.UserAbstract.HotelOwnerUserDto;
import com.backend.KKUN_Booking.exception.ResourceNotFoundException;
import com.backend.KKUN_Booking.exception.UserAlreadyExistsException;
import com.backend.KKUN_Booking.model.*;
import com.backend.KKUN_Booking.model.UserAbstract.AdminUser;
import com.backend.KKUN_Booking.model.UserAbstract.CustomerUser;
import com.backend.KKUN_Booking.model.UserAbstract.HotelOwnerUser;
import com.backend.KKUN_Booking.model.enumModel.RoleUser;
import com.backend.KKUN_Booking.model.enumModel.UserStatus;
import com.backend.KKUN_Booking.repository.RoleRepository;
import com.backend.KKUN_Booking.repository.UserRepository;
import com.backend.KKUN_Booking.service.UserService;
import com.backend.KKUN_Booking.util.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Primary
public class UserServiceImpl implements UserService, UserDetailsService {

    @Autowired
    private UserRepository userRepository; // Inject your user repository here
    @Autowired
    private RoleRepository roleRepository; // Inject your role repository here
    @Autowired
    private PasswordEncoder passwordEncoder; // Inject your password encoder here

    private final int MAX_RECENT_SEARCHES = 10;
    private final int MAX_SAVED_HOTELS = 20;

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already in use");
        }

        // Proceed with user creation
        User user = convertToEntity(userDto);
        user.setCreatedDate(LocalDateTime.now());
        return convertToDto(userRepository.save(user));
    }

    @Override
    public UserDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return convertToDto(user);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return convertToDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto updateUser(UUID id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        updateUserFromDto(user, userDto);
        user = userRepository.save(user);
        return convertToDto(user);
    }

    @Override
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
    }

    // Conversion methods

    private User convertToEntity(UserDto userDto) {
        User user;
        // Get the role by roleId
        Role role = roleRepository.findById(userDto.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        // Determine user type based on role ID
        if (role.getName().equals(RoleUser.ADMIN.name())) {
            user = new AdminUser();
            AdminUserDto adminUserDto = (AdminUserDto) userDto;
            ((AdminUser) user).setManagedSections(adminUserDto.getManagedSections());
            ((AdminUser) user).setActionCount(adminUserDto.getActionCount());
        } else if (role.getName().equals(RoleUser.CUSTOMER.name())) {
            user = new CustomerUser();
            CustomerUserDto customerUserDto = (CustomerUserDto) userDto;
            // Additional mapping if needed
        } else if (role.getName().equals(RoleUser.HOTELOWNER.name())) {
            user = new HotelOwnerUser();
            HotelOwnerUserDto hotelOwnerUserDto = (HotelOwnerUserDto) userDto;
            // Additional mapping if needed
        } else {
            throw new IllegalArgumentException("Unknown role type");
        }

        // Set common user properties
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setAlias(CommonFunction.generateAlias(userDto.getFirstName(),userDto.getLastName()));
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(role);

        // Hash the password (use your preferred method)
        String hashedPassword = passwordEncoder.encode(userDto.getPassword());
        user.setPassword(hashedPassword);

        return user;
    }

    private UserDto convertToDto(User user) {
        UserDto userDto;

        // Get the role name
        String roleName = user.getRole() != null ? user.getRole().getName() : null;

        // Determine DTO type based on role name
        if (RoleUser.ADMIN.name().equals(roleName)) {
            AdminUser adminUser = (AdminUser) user;
            AdminUserDto adminUserDto = new AdminUserDto();
            adminUserDto.setManagedSections(adminUser.getManagedSections());
            adminUserDto.setActionCount(adminUser.getActionCount());
            userDto = adminUserDto;
        } else if (RoleUser.CUSTOMER.name().equals(roleName)) {
            CustomerUser customerUser = (CustomerUser) user;
            CustomerUserDto customerUserDto = new CustomerUserDto();
            customerUserDto.setBookingIds(customerUser.getBookings()
                    .stream()
                    .map(Booking::getId)
                    .collect(Collectors.toList()));
            userDto = customerUserDto;
        } else if (RoleUser.HOTELOWNER.name().equals(roleName)) {
            HotelOwnerUser hotelOwnerUser = (HotelOwnerUser) user;
            HotelOwnerUserDto hotelOwnerUserDto = new HotelOwnerUserDto();
            hotelOwnerUserDto.setHotelId(hotelOwnerUser.getHotel() != null ? hotelOwnerUser.getHotel().getId() : null);
            hotelOwnerUserDto.setHotelName(hotelOwnerUser.getHotel() != null ? hotelOwnerUser.getHotel().getName() : null);
            userDto = hotelOwnerUserDto;
        } else {
            throw new IllegalArgumentException("Unknown user type");
        }

        // Set common user properties
        userDto.setId(user.getId());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());
        userDto.setAlias(user.getAlias());
        userDto.setCreatedDate(user.getCreatedDate());
        userDto.setStatus(user.getStatus());
        userDto.setRoleId(user.getRole() != null ? user.getRole().getId() : null);
        userDto.setPassword(user.getPassword());
        return userDto;
    }

    private void updateUserFromDto(User user, UserDto userDto) {
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setAlias(userDto.getAlias());

        if (userDto.getStatus() != null) {
            user.setStatus(userDto.getStatus());
        }

        if (userDto.getRoleId() != null) {
            Role role = roleRepository.findById(userDto.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
            user.setRole(role);
        }

        // Update specific fields based on user type
        if (user instanceof AdminUser && userDto instanceof AdminUserDto) {
            AdminUser adminUser = (AdminUser) user;
            AdminUserDto adminUserDto = (AdminUserDto) userDto;
            adminUser.setManagedSections(adminUserDto.getManagedSections());
            adminUser.setActionCount(adminUserDto.getActionCount());
        } else if (user instanceof CustomerUser && userDto instanceof CustomerUserDto) {
            CustomerUser customerUser = (CustomerUser) user;
            CustomerUserDto customerUserDto = (CustomerUserDto) userDto;
            // Update customer specific fields if necessary
        } else if (user instanceof HotelOwnerUser && userDto instanceof HotelOwnerUserDto) {
            HotelOwnerUser hotelOwnerUser = (HotelOwnerUser) user;
            HotelOwnerUserDto hotelOwnerUserDto = (HotelOwnerUserDto) userDto;
            // Update hotel owner specific fields if necessary
        }
    }

    @Override
    public void saveOauthUser(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("Email already registered.");
        }
        User user = new CustomerUser(); // Assuming OAuth users are customers by default
        user.setEmail(email);
        user.setCreatedDate(LocalDateTime.now());
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setAlias(CommonFunction.saveAliasAccount(email));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getAuthorities())
                .accountExpired(!user.isAccountNonExpired())
                .accountLocked(!user.isAccountNonLocked())
                .credentialsExpired(!user.isCredentialsNonExpired())
                .disabled(!user.isEnabled())
                .build();
    }

    @Transactional
    public void addRecentSearch(UUID userId, String searchTerm) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> recentSearches = user.getRecentSearches();
        if (recentSearches == null) {
            recentSearches = new ArrayList<>();
        }

        // Remove the search term if it already exists
        recentSearches.remove(searchTerm);

        // Add the new search term at the beginning of the list
        recentSearches.add(0, searchTerm);

        // Trim the list if it exceeds the maximum size
        if (recentSearches.size() > MAX_RECENT_SEARCHES) {
            recentSearches = recentSearches.subList(0, MAX_RECENT_SEARCHES);
        }

        user.setRecentSearches(recentSearches);
        userRepository.save(user);
    }

    @Transactional
    public void saveSaveHotel(UUID userId, UUID hotelId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> savedHotels = user.getSavedHotels();
        if (savedHotels == null) {
            savedHotels = new ArrayList<>();
        }
        String hotelIdString = hotelId.toString();
        if (!savedHotels.contains(hotelIdString)) {
            savedHotels.add(hotelIdString);

            // Trim the list if it exceeds the maximum size
            if (savedHotels.size() > MAX_SAVED_HOTELS) {
                savedHotels.remove(0);
            }

            user.setSavedHotels(savedHotels);
            userRepository.save(user);
        }
    }

    @Transactional
    public void removeSavedHotel(UUID userId, UUID hotelId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> savedHotels = user.getSavedHotels();
        if (savedHotels != null && savedHotels.remove(hotelId.toString())) {
            user.setSavedHotels(savedHotels);
            userRepository.save(user);
        }
    }

    public List<String> getRecentSearches(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getRecentSearches();
    }

    public List<String> getSavedHotels(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getSavedHotels();
    }


}