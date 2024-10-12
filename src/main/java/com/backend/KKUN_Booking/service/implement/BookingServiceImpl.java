package com.backend.KKUN_Booking.service.implement;

import com.backend.KKUN_Booking.dto.BookingDto;
import com.backend.KKUN_Booking.dto.PaymentDto;
import com.backend.KKUN_Booking.exception.ResourceNotFoundException;
import com.backend.KKUN_Booking.model.Booking;
import com.backend.KKUN_Booking.model.Payment;
import com.backend.KKUN_Booking.model.Room;
import com.backend.KKUN_Booking.model.User;
import com.backend.KKUN_Booking.model.enumModel.BookingStatus;
import com.backend.KKUN_Booking.model.enumModel.PaymentPolicy;
import com.backend.KKUN_Booking.model.enumModel.PaymentStatus;
import com.backend.KKUN_Booking.model.enumModel.PaymentType;
import com.backend.KKUN_Booking.model.reviewAbstract.RoomReview;
import com.backend.KKUN_Booking.repository.*;
import com.backend.KKUN_Booking.response.PaymentResponse;
import com.backend.KKUN_Booking.service.BookingService;
import com.backend.KKUN_Booking.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final RoomRepository roomRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;

    public BookingServiceImpl(BookingRepository bookingRepository, RoomRepository roomRepository, PaymentRepository paymentRepository, ReviewRepository reviewRepository, UserRepository userRepository, PaymentService paymentService) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.paymentRepository = paymentRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.paymentService = paymentService;
    }

    @Override
    public List<BookingDto> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public BookingDto getBookingById(UUID id) {
        return bookingRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    @Override
    public BookingDto createBooking(BookingDto bookingDto, String userEmail) {
        // Fetch the user by email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Fetch the room by ID
        Room room = roomRepository.findById(bookingDto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        // Check if the room is already booked for the requested dates
        if (isRoomAlreadyBooked(room, bookingDto.getCheckinDate(), bookingDto.getCheckoutDate())) {
            throw new IllegalStateException("Room is already booked for the selected dates.");
        }

        // Calculate total price
        double totalPrice = calculateTotalPrice(room.getBasePrice(), bookingDto.getCheckinDate(), bookingDto.getCheckoutDate());

        bookingDto.setTotalPrice(totalPrice);
        // Convert DTO to entity
        Booking booking = convertToEntity(bookingDto);
        booking.setUser(user);
        booking.setRoom(room);
        booking.setReviewed(false);
        if (room.getHotel().getPaymentPolicy() == PaymentPolicy.CHECKOUT) {
            try {
                // Save the booking
                booking.setStatus(BookingStatus.PAY_ON_CHECKOUT);
                booking.getPayment().setPaymentType(PaymentType.POC);
            } catch (Exception e) {
                throw new IllegalStateException("Error during payment processing: " + e.getMessage(), e);
            }
        } else {
            // Set status to pending as payment will be handled at checkout
            booking.setStatus(BookingStatus.PENDING);
        }
        Booking savedBooking = bookingRepository.save(booking);
        return convertToDto(savedBooking);
    }
    public void markBookingAsReviewed(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        booking.setReviewed(true);
        booking.setCreatedDate(LocalDateTime.now());
        bookingRepository.save(booking);
    }
    public List<Booking> getCompletedBookingsWithoutReview() {
        LocalDateTime now = LocalDateTime.now();
        return bookingRepository.findByCheckoutDateBeforeAndReviewedFalse(now);
    }
    private boolean isRoomAlreadyBooked(Room room, LocalDateTime checkinDate, LocalDateTime checkoutDate) {
        // Query the database for any existing bookings for the same room that overlap with the requested dates
        List<Booking> existingBookings = bookingRepository.findByRoomAndDateRange(room, checkinDate, checkoutDate);

        // If there are any overlapping bookings, return true
        return !existingBookings.isEmpty();
    }
    // Method to calculate total price based on the number of nights
    private double calculateTotalPrice(double basePrice, LocalDateTime checkinDate, LocalDateTime checkoutDate) {
        // Chuyển đổi sang LocalDate để chỉ tính theo ngày
        LocalDate checkin = checkinDate.toLocalDate();
        LocalDate checkout = checkoutDate.toLocalDate();

        long numberOfNights = ChronoUnit.DAYS.between(checkin, checkout);
        double totalPrice = numberOfNights * basePrice;


        return totalPrice;
    }


    @Override
    public PaymentResponse.BaseResponse initiatePayment(UUID bookingId, PaymentType paymentType, HttpServletRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Booking is not in PENDING state");
        }

        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setAmount(booking.getTotalPrice());
        paymentDto.setPaymentType(paymentType);
        paymentDto.setBookingId(bookingId);

        return paymentService.initiatePayment(paymentDto, request);
    }

    @Override
    public void updateBookingStatus(UUID bookingId, BookingStatus status) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        booking.setStatus(status);
        booking.getPayment().setStatus(PaymentStatus.COMPLETED);
        bookingRepository.save(booking);
    }

    @Override
    public BookingDto updateBooking(UUID id, BookingDto bookingDto, String userEmail) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You do not have permission to update this booking.");
        }

        // Update booking information
        booking.setCheckinDate(bookingDto.getCheckinDate());
        booking.setCheckoutDate(bookingDto.getCheckoutDate());
        booking.setStatus(bookingDto.getStatus());

        return convertToDto(bookingRepository.save(booking));
    }

    @Override
    public void deleteBooking(UUID id) {
        if (!bookingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Booking not found");
        }
        bookingRepository.deleteById(id);
    }

    private BookingDto convertToDto(Booking booking) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(booking.getId());
        bookingDto.setCheckinDate(booking.getCheckinDate());
        bookingDto.setCheckoutDate(booking.getCheckoutDate());
        bookingDto.setTotalPrice(booking.getTotalPrice());
        bookingDto.setStatus(booking.getStatus());
        bookingDto.setPaymentType(booking.getPayment().getPaymentType());
        bookingDto.setCreatedDate(booking.getCreatedDate());
        bookingDto.setUpdatedDate(booking.getUpdatedDate());
        // Set associated foreign keys for User, Room, and Payment (if they exist)
        if (booking.getUser() != null) {
            bookingDto.setUserId(booking.getUser().getId());
        }
        if (booking.getRoom() != null) {
            bookingDto.setRoomId(booking.getRoom().getId());
        }
        if (booking.getPayment() != null) {
            bookingDto.setPayment(booking.getPayment());
        }

        return bookingDto;
    }


    private Booking convertToEntity(BookingDto bookingDto) {
        Booking booking = new Booking();
        booking.setCheckinDate(bookingDto.getCheckinDate());
        booking.setCheckoutDate(bookingDto.getCheckoutDate());
        booking.setTotalPrice(bookingDto.getTotalPrice());
        booking.setStatus(bookingDto.getStatus() != null ? bookingDto.getStatus() : BookingStatus.PENDING); // Default to PENDING
        booking.setCreatedDate(LocalDateTime.now()); // Set current timestamp for creation
        booking.setUpdatedDate(LocalDateTime.now()); // Set current timestamp for updates
        if(bookingDto.getPayment() == null){
            // Check if payment object is null and initialize it if necessary
            Payment payment = new Payment();
            payment.setPaymentDate(LocalDateTime.now());
            payment.setAmount(bookingDto.getTotalPrice());
            payment.setPaymentType(bookingDto.getPaymentType()); // Ensure you set the required properties
            payment.setStatus(PaymentStatus.PENDING);
            payment.setBooking(booking); // Associate the payment with the booking
            // Set other properties if necessary
            booking.setPayment(payment);

        }
        return booking;
    }

}
