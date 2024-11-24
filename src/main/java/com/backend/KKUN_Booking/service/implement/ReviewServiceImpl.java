package com.backend.KKUN_Booking.service.implement;

import com.backend.KKUN_Booking.dto.ReviewDto;
import com.backend.KKUN_Booking.dto.UserDto;
import com.backend.KKUN_Booking.dto.abstractDto.ReviewAbstract.RoomReviewDto;
import com.backend.KKUN_Booking.dto.abstractDto.ReviewAbstract.TouringReviewDto;
import com.backend.KKUN_Booking.dto.abstractDto.UserAbstract.CustomerUserDto;
import com.backend.KKUN_Booking.dto.abstractDto.UserAbstract.HotelOwnerUserDto;
import com.backend.KKUN_Booking.exception.CustomBadRequestException;
import com.backend.KKUN_Booking.exception.ResourceNotFoundException;
import com.backend.KKUN_Booking.model.Booking;
import com.backend.KKUN_Booking.model.Review;
import com.backend.KKUN_Booking.model.Room;
import com.backend.KKUN_Booking.model.User;
import com.backend.KKUN_Booking.model.enumModel.BookingStatus;
import com.backend.KKUN_Booking.model.enumModel.RoleUser;
import com.backend.KKUN_Booking.model.reviewAbstract.RoomReview;
import com.backend.KKUN_Booking.model.reviewAbstract.TouringReview;
import com.backend.KKUN_Booking.repository.*;
import com.backend.KKUN_Booking.service.BookingService;
import com.backend.KKUN_Booking.service.ReviewService;
import com.backend.KKUN_Booking.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final TouringRepository touringRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    @Autowired
    private UserService userService;
    public ReviewServiceImpl(ReviewRepository reviewRepository, UserRepository userRepository,
                             RoomRepository roomRepository, TouringRepository touringRepository,BookingRepository bookingRepository, BookingService bookingService) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.touringRepository = touringRepository;
        this.bookingRepository = bookingRepository;
        this.bookingService = bookingService;
    }

    @Override
    public List<ReviewDto> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ReviewDto getReviewById(UUID id) {
        return reviewRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
    }
    @Override
    public List<ReviewDto>  getReviewsByRoomId(UUID roomId) {
        return bookingRepository.findByRoomId(roomId).stream()
                .filter(booking -> booking.getReview() != null) // Lọc các booking có review
                .map(booking -> convertToDto(booking.getReview())) // Chuyển đổi Review thành ReviewDto
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReviewDto createReview(UUID bookingId, ReviewDto reviewDto) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Nếu không phải đánh giá ẩn danh, kiểm tra quyền truy cập
        if (!reviewDto.isAnonymous()) {
            if (reviewDto.getUser() == null || !booking.getUser().getId().equals(reviewDto.getUser().getId())) {
                throw new CustomBadRequestException("You did not book this booking!");
            }
        }

        // Kiểm tra trạng thái của booking
        if (booking.isReviewed() && booking.getStatus() == BookingStatus.CONFIRMED) {
            throw new CustomBadRequestException("Cannot review this booking. This booking has already been reviewed!");
        }

        // Xử lý đánh giá
        ReviewDto createdReview;
        if (reviewDto instanceof RoomReviewDto) {
            createdReview = createRoomReview((RoomReviewDto) reviewDto, booking);
            Room room = booking.getRoom();
            if (room != null && room.getHotel() != null) {
                room.getHotel().updateRating(); // Cập nhật rating cho khách sạn
                room.getHotel().updateNumOfReviews();
            }
        } else if (reviewDto instanceof TouringReviewDto) {
            createdReview = createTouringReview((TouringReviewDto) reviewDto, booking);
        } else {
            throw new CustomBadRequestException("Invalid review type");
        }

        bookingService.markBookingAsReviewed(bookingId);
        return createdReview;
    }


    private ReviewDto createRoomReview(RoomReviewDto roomReviewDto, Booking booking) {
        RoomReview review = new RoomReview();
        roomReviewDto.setReviewerName(booking.getBookingName());
        roomReviewDto.setRoomId(booking.getRoom().getId());
        populateRoomReview(roomReviewDto, review);
        review.setBooking(booking);
        // Assuming room has a method to set the hotel (or get it through a relationship)
        Room room = roomRepository.findById(booking.getRoom().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        room.getReviews().add(review); // Add the review to the room (if you have a list of reviews in Room)

        reviewRepository.save(review); // Save the review

        return convertToDto(reviewRepository.save(review));
    }

    private ReviewDto createTouringReview(TouringReviewDto touringReviewDto, Booking booking) {
        TouringReview review = new TouringReview();
        review.setReviewerName(booking.getBookingName());
        populateTouringReview(touringReviewDto, review);
        review.setBooking(booking);
        return convertToDto(reviewRepository.save(review));
    }

    @Override
    public ReviewDto updateReview(UUID id, ReviewDto reviewDto) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (review instanceof RoomReview && reviewDto instanceof RoomReviewDto) {
            updateRoomReview((RoomReview) review, (RoomReviewDto) reviewDto);
        } else if (review instanceof TouringReview && reviewDto instanceof TouringReviewDto) {
            updateTouringReview((TouringReview) review, (TouringReviewDto) reviewDto);
        } else {
            throw new IllegalArgumentException("Invalid review type for update");
        }

        return convertToDto(reviewRepository.save(review));
    }

    @Override
    public void deleteReview(UUID id) {
        reviewRepository.deleteById(id);
    }



    private void populateRoomReview(RoomReviewDto roomReviewDto, RoomReview review) {
        review.setRoom(roomRepository.findById(roomReviewDto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found")));

        populateCommonFields(roomReviewDto, review);

        review.setCleanliness(roomReviewDto.getCleanliness());
        review.setAmenities(roomReviewDto.getAmenities());
        review.setSpace(roomReviewDto.getSpace());
        review.setComfort(roomReviewDto.getComfort());
        review.setValueForMoney(roomReviewDto.getValueForMoney());

        review.updateOverallRating();
    }

    private void populateTouringReview(TouringReviewDto touringReviewDto, TouringReview review) {
        review.setTouring(touringRepository.findById(touringReviewDto.getTouringId())
                .orElseThrow(() -> new ResourceNotFoundException("Touring not found")));

        populateCommonFields(touringReviewDto, review);

        review.setItinerary(touringReviewDto.getItinerary());
        review.setAttractions(touringReviewDto.getAttractions());
        review.setTourGuide(touringReviewDto.getTourGuide());
        review.updateOverallRating();

    }

    private void populateCommonFields(ReviewDto reviewDto, Review review) {
        review.setOverallRating(reviewDto.getOverallRating());
        review.setComment(reviewDto.getComment());
        review.setDate(LocalDateTime.now());

        // Nếu là đánh giá ẩn danh, chỉ lưu tên người đánh giá (nếu có)
        if (reviewDto.isAnonymous()) {
            review.setReviewerName(reviewDto.getReviewerName());
            review.setAnonymous(true);
        } else {
            // Nếu không ẩn danh, gán thông tin người dùng
            review.setUser(userRepository.findById(reviewDto.getUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found")));
        }
    }

    private void updateRoomReview(RoomReview roomReview, RoomReviewDto roomReviewDto) {
        roomReview.setCleanliness(roomReviewDto.getCleanliness());
        roomReview.setAmenities(roomReviewDto.getAmenities());
        roomReview.setSpace(roomReviewDto.getSpace());
        roomReview.setComfort(roomReviewDto.getComfort());
        updateCommonFields(roomReview, roomReviewDto);
    }

    private void updateTouringReview(TouringReview touringReview, TouringReviewDto touringReviewDto) {
        touringReview.setItinerary(touringReviewDto.getItinerary());
        touringReview.setAttractions(touringReviewDto.getAttractions());
        touringReview.setTourGuide(touringReviewDto.getTourGuide()); // Corrected line
        updateCommonFields(touringReview, touringReviewDto);
    }

    private void updateCommonFields(Review review, ReviewDto dto) {
        review.setOverallRating(dto.getOverallRating());
        review.setComment(dto.getComment());
        review.setDate(dto.getDate());
    }

    private ReviewDto convertToDto(Review review) {
        if (review instanceof RoomReview) {
            return convertToRoomReviewDto((RoomReview) review);
        } else if (review instanceof TouringReview) {
            return convertToTouringReviewDto((TouringReview) review);
        }
        throw new IllegalArgumentException("Unsupported review type");
    }

    private RoomReviewDto convertToRoomReviewDto(RoomReview review) {
        RoomReviewDto dto = new RoomReviewDto();
        populateCommonDtoFields(dto, review);
        dto.setReviewerName(review.getReviewerName());
        dto.setUser(convertUserToDto(review.getUser()));
        dto.setRoomId(review.getRoom().getId());
        dto.setCleanliness(review.getCleanliness());
        dto.setAmenities(review.getAmenities());
        dto.setSpace(review.getSpace());
        dto.setComfort(review.getComfort());
        dto.setValueForMoney(review.getValueForMoney());

        return dto;
    }

    private TouringReviewDto convertToTouringReviewDto(TouringReview review) {
        TouringReviewDto dto = new TouringReviewDto();
        populateCommonDtoFields(dto, review);
        dto.setReviewerName(review.getReviewerName());
        dto.setTouringId(review.getTouring().getId());
        dto.setItinerary(review.getItinerary());
        dto.setAttractions(review.getAttractions());
        dto.setTourGuide(review.getTourGuide());
        return dto;
    }

    private void populateCommonDtoFields(ReviewDto dto, Review review) {
        dto.setId(review.getId());
        dto.setOverallRating(review.getOverallRating());
        dto.setComment(review.getComment());
        dto.setDate(review.getDate());
        dto.setUser(convertUserToDto(review.getUser()));
    }
    private UserDto convertUserToDto(User user) {
        if (user == null) return null;
        UserDto userDto;
        if (user.getRole().getName().equals(RoleUser.HOTELOWNER.name())) {
            userDto = new HotelOwnerUserDto();
        } else {
            userDto = new CustomerUserDto(); // Giả định là khách hàng nếu không phải admin hoặc hotel owner
        }
        userDto.setId(user.getId());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());
        userDto.setAddress(user.getAddress());
        userDto.setPhone(user.getPhone());
        userDto.setAvatar(user.getAvatar());
        userDto.setAlias(user.getAlias());


        return userDto;
    }

}