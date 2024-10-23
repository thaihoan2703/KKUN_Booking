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
import com.backend.KKUN_Booking.model.enumModel.AuthProvider;
import com.backend.KKUN_Booking.model.enumModel.RoleUser;
import com.backend.KKUN_Booking.model.enumModel.UserStatus;
import com.backend.KKUN_Booking.repository.RoleRepository;
import com.backend.KKUN_Booking.repository.UserRepository;
import com.backend.KKUN_Booking.security.UserDetailsImpl;
import com.backend.KKUN_Booking.service.AmazonS3Service;
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
import org.springframework.web.multipart.MultipartFile;

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
    private final AmazonS3Service amazonS3Service;

    private final int MAX_RECENT_SEARCHES = 10;
    private final int MAX_SAVED_HOTELS = 20;

    public UserServiceImpl(AmazonS3Service amazonS3Service) {
        this.amazonS3Service = amazonS3Service;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already in use");
        }

        // Proceed with user creation
        User user = convertToEntity(userDto);
        user.setCreatedDate(LocalDateTime.now());
        user.setAuthProvider(AuthProvider.LOCAL);
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
    public UserDto updateUser(UUID id, UserDto userDto, MultipartFile profileImage) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        try {
            updateUserFromDto(user, userDto, profileImage);
            user = userRepository.save(user);
            return convertToDto(user);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Invalid user type for update", e);
        }
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
        Role role;
        if (userDto instanceof CustomerUserDto) {
            user = new CustomerUser();
            role = roleRepository.findByName(RoleUser.CUSTOMER.name())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
            // Additional mapping if needed
        } else if (userDto instanceof HotelOwnerUserDto) {
            user = new HotelOwnerUser();
            role = roleRepository.findByName(RoleUser.HOTELOWNER.name())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
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
        user.setAddress(userDto.getAddress());
        user.setPhone(userDto.getPhone());
        user.setRole(role);

        // Hash the password (use your preferred method)
        String hashedPassword = passwordEncoder.encode(userDto.getPassword());
        user.setPassword(hashedPassword);

        return user;
    }

    private String saveProfileImage(UserDto userDto,MultipartFile profileImage){
        String s3ImageUrl="";
        if (profileImage != null ) {
            if(userDto.getId() != null){
                String profileImageOld = userDto.getAvatar();
                if(profileImageOld != null){
                    amazonS3Service.deleteFile(profileImageOld);
                }
                String uniqueFileName = CommonFunction.SEOUrl(userDto.getEmail()) + "-" + userDto.getId();
                s3ImageUrl = amazonS3Service.uploadAvatarUserFile(profileImage, uniqueFileName);

            }
        }
        return s3ImageUrl;
    };

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
        userDto.setPhone(user.getPhone());
        userDto.setAddress(user.getAddress());
        userDto.setAvatar(user.getAvatar());
        userDto.setCreatedDate(user.getCreatedDate());
        userDto.setStatus(user.getStatus());
        userDto.setRoleId(user.getRole() != null ? user.getRole().getId() : null);
        userDto.setPassword(user.getPassword());
        userDto.setHasPassword(user.getPassword() != null && !user.getPassword().isEmpty());
        userDto.setAuthProvider(user.getAuthProvider());
        return userDto;
    }

    private void updateUserFromDto(User user, UserDto userDto, MultipartFile profileImage) {
        // Update common fields first
        updateCommonFields(user, userDto, profileImage);

        // Then update type-specific fields
        try {
            if (user instanceof AdminUser && userDto instanceof AdminUserDto) {
                updateAdminFields((AdminUser) user, (AdminUserDto) userDto);
            } else if (user instanceof CustomerUser && userDto instanceof CustomerUserDto) {
                updateCustomerFields((CustomerUser) user, (CustomerUserDto) userDto);
            } else if (user instanceof HotelOwnerUser && userDto instanceof HotelOwnerUserDto) {
                updateHotelOwnerFields((HotelOwnerUser) user, (HotelOwnerUserDto) userDto);
            }
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Invalid user type combination", e);
        }
    }
    private void updateCommonFields(User user, UserDto userDto, MultipartFile profileImage) {
        if (userDto.getFirstName() != null) user.setFirstName(userDto.getFirstName());
        if (userDto.getLastName() != null) user.setLastName(userDto.getLastName());
        if (userDto.getAlias() != null) user.setAlias(userDto.getAlias());
        if (userDto.getAddress() != null) user.setAddress(userDto.getAddress());
        if (userDto.getPhone() != null) user.setPhone(userDto.getPhone());

        if (profileImage != null && !profileImage.isEmpty()) {
            String imagePath = saveProfileImage(userDto, profileImage);
            user.setAvatar(imagePath);
        }

        if (userDto.getStatus() != null) user.setStatus(userDto.getStatus());

        if (userDto.getRoleId() != null) {
            Role role = roleRepository.findById(userDto.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
            user.setRole(role);
        }

        if (userDto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
    }
    private void updateAdminFields(AdminUser user, AdminUserDto dto) {

    }

    private void updateCustomerFields(CustomerUser user, CustomerUserDto dto) {
        // Update customer specific fields
    }

    private void updateHotelOwnerFields(HotelOwnerUser user, HotelOwnerUserDto dto) {
        // Update hotel owner specific fields
    }
    @Override
    public User findOrSaveOauthUser(String email, String name) {
        // Tìm kiếm người dùng bằng email
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            // Nếu người dùng đã tồn tại, trả về đối tượng người dùng
            return optionalUser.get();
        } else {
            // Nếu người dùng chưa tồn tại, tạo người dùng mới
            User newUser = new CustomerUser();
            newUser.setEmail(email);
            newUser.setFirstName(name); // Lưu tên hoặc bạn có thể tách họ và tên riêng ra
            newUser.setAuthProvider(AuthProvider.GOOGLE); // Đặt kiểu xác thực là Google
            newUser.setCreatedDate(LocalDateTime.now());
            newUser.setStatus(UserStatus.ACTIVE);

            // Gán vai trò mặc định cho người dùng, ví dụ "ROLE_USER"
            Role userRole = roleRepository.findByName(RoleUser.CUSTOMER.name())
                    .orElseThrow(() -> new RuntimeException("User Role not found"));
            newUser.setRole(userRole);

            // Lưu người dùng mới vào cơ sở dữ liệu
            return userRepository.save(newUser);
        }
    }

    @Override
    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Kiểm tra nếu người dùng đã đăng nhập bằng OAuth (Google, Facebook, v.v.)
        if (user.getAuthProvider() == AuthProvider.GOOGLE) {
            // Nếu là tài khoản Google và chưa bao giờ thiết lập mật khẩu trước đó
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                // Người dùng không có mật khẩu ban đầu, cho phép thiết lập mật khẩu mới
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return;
            } else {
                // Nếu là tài khoản Google và đã có mật khẩu trước đó, yêu cầu mật khẩu cũ
                if (oldPassword == null || !passwordEncoder.matches(oldPassword, user.getPassword())) {
                    throw new IllegalArgumentException("Mật khẩu cũ không chính xác");
                }
            }
        } else {
            // Nếu là người dùng thông thường, kiểm tra mật khẩu cũ
            if (oldPassword == null || !passwordEncoder.matches(oldPassword, user.getPassword())) {
                throw new IllegalArgumentException("Mật khẩu cũ không chính xác");
            }
        }

        // Mã hóa và cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }



    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return UserDetailsImpl.build(user);
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