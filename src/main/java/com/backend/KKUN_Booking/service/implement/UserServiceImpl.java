package com.backend.KKUN_Booking.service.implement;

import com.backend.KKUN_Booking.dto.UserDto;
import com.backend.KKUN_Booking.exception.ResourceNotFoundException;
import com.backend.KKUN_Booking.exception.UserAlreadyExistsException;
import com.backend.KKUN_Booking.model.Role;
import com.backend.KKUN_Booking.model.User;
import com.backend.KKUN_Booking.repository.RoleRepository;
import com.backend.KKUN_Booking.repository.UserRepository;
import com.backend.KKUN_Booking.service.UserService;
import com.backend.KKUN_Booking.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Primary
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private  final RoleRepository roleRepository;


    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> convertToDto(user))
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(UUID id) {
        return userRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        // Check if a user with the same email already exists
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already in use"); // Custom exception
        }
        User user = convertToEntity(userDto);
        user.setCreatedDate(LocalDateTime.now()); // Set the created date

        // Find role by ID
        Role role = roleRepository.findById(userDto.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found")); // Ensure Role exists
        user.setRole(role);

        return convertToDto(userRepository.save(user));
    }

    @Override
    public UserDto updateUser(UUID id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setAlias(userDto.getAlias());
        // Chuyển đổi status từ UserDto sang User
        if (userDto.getStatus() != null) {
            user.setStatus(userDto.getStatus());
        }
        // Lấy Role từ roleId
        if (userDto.getRoleId() != null) {
            Role role = roleRepository.findById(userDto.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
            user.setRole(role);
        }
        return convertToDto(userRepository.save(user));
    }

    @Override
    public void saveOauthUser(String email) {
        // Check if the email already exists
        Optional<User> existingUserByEmail = userRepository.findByEmail(email);
        if (existingUserByEmail.isPresent()) {
            throw new UserAlreadyExistsException("Email đã được đăng ký.");
        }

        // Create a new user
        User user = new User();
        user.setLastName(email);
        user.setEmail(email);
        user.setCreatedDate(LocalDateTime.now());

        // Generate a random password or leave it empty if not needed
        user.setPassword(UUID.randomUUID().toString()); // Or generate a random password if necessary


        // Set alias
        user.setAlias(CommonFunction.saveAliasAccount(user.getEmail()));

        // Save the user
        userRepository.save(user);
    }


    @Override
    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }
    @Override
    public UserDetails loadUserByUsername(String email) throws
            UsernameNotFoundException {
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
    private UserDto convertToDto(User user) {
        // Convert User entity to UserDto
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());
        userDto.setPassword(user.getPassword());

        userDto.setAlias(user.getAlias());

        // Chuyển đổi từ enum UserStatus thành chuỗi
        userDto.setStatus(user.getStatus());

        // Lấy roleId từ đối tượng Role
        if (user.getRole() != null) {
            userDto.setRoleId(user.getRole().getId());
        }

        return userDto;
    }

    private User convertToEntity(UserDto userDto) {
        User user = new User();
        user.setId(userDto.getId());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setCreatedDate(LocalDateTime.now()); // Set the created date
        // Hash the password (use your preferred method)
        String hashedPassword = passwordEncoder.encode(userDto.getPassword());
        user.setPassword(hashedPassword);

        user.setAlias(userDto.getAlias());

        // Chuyển đổi từ enum UserStatus
        user.setStatus(userDto.getStatus());

        // Lấy đối tượng Role từ roleId nếu có
        if (userDto.getRoleId() != null) {
            Role role = roleRepository.findById(userDto.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
            user.setRole(role);
        }

        return user;
    }

}

