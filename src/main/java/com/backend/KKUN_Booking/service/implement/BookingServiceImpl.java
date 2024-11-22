package com.backend.KKUN_Booking.service.implement;

import com.backend.KKUN_Booking.dto.BookingDto;
import com.backend.KKUN_Booking.dto.HotelDto;
import com.backend.KKUN_Booking.dto.PaymentDto;
import com.backend.KKUN_Booking.dto.PriceCalculationResult;
import com.backend.KKUN_Booking.exception.ResourceNotFoundException;
import com.backend.KKUN_Booking.model.*;
import com.backend.KKUN_Booking.model.enumModel.*;
import com.backend.KKUN_Booking.model.reviewAbstract.RoomReview;
import com.backend.KKUN_Booking.repository.*;
import com.backend.KKUN_Booking.response.PaymentResponse;
import com.backend.KKUN_Booking.service.BookingService;
import com.backend.KKUN_Booking.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final RoomRepository roomRepository;

    private final ReviewRepository reviewRepository;
    private final PromotionRepository promotionRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;

    public BookingServiceImpl(BookingRepository bookingRepository, RoomRepository roomRepository, PaymentRepository paymentRepository, ReviewRepository reviewRepository, PromotionRepository promotionRepository, HotelRepository hotelRepository, UserRepository userRepository, PaymentService paymentService) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.paymentRepository = paymentRepository;
        this.reviewRepository = reviewRepository;
        this.promotionRepository = promotionRepository;
        this.hotelRepository = hotelRepository;
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
        User user = null;

        // Nếu không phải user ẩn danh, tìm kiếm user trong cơ sở dữ liệu
        if (userEmail != null && !userEmail.equals("anonymous@domain.com")) {
            user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }

        // Fetch the room by ID
        Room room = roomRepository.findById(bookingDto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        // Check if the room is already booked for the requested dates
        if (isRoomAlreadyBooked(room, bookingDto.getCheckinDate(), bookingDto.getCheckoutDate())) {
            throw new IllegalStateException("Phòng này đã được đặt trước vào khoảng thời gian bạn chọn!");
        }

        // Tìm `Promotion` nếu có
        Promotion promotion = null;
        if (bookingDto.getPromotionId() != null) {
            promotion = promotionRepository.findById(bookingDto.getPromotionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));
        }

        // Tính tổng giá và các khoản phí
        BigDecimal basePrice = room.getBasePrice();
        BigDecimal taxRate = new BigDecimal("0.1");          // Ví dụ: 10% thuế
        BigDecimal serviceFeeRate = BigDecimal.ZERO;         // Ví dụ: 5% phí dịch vụ

        PriceCalculationResult priceCalculation = calculateTotalPrice(basePrice, bookingDto.getCheckinDate(),
                bookingDto.getCheckoutDate(), taxRate, serviceFeeRate, promotion);

        // Gán thông tin vào DTO
        bookingDto.setTotalPrice(priceCalculation.getTotalPrice());
        bookingDto.setDiscount(priceCalculation.getDiscountAmount()); // Giá trị giảm giá
        bookingDto.setTaxRate(priceCalculation.getTaxAmount());       // Thuế
        bookingDto.setServiceFeeRate(priceCalculation.getServiceFee()); // Phí dịch vụ

        // Convert DTO to entity
        Booking booking = convertToEntity(bookingDto);
        if (user != null) {
            booking.setUser(user);
        }
        booking.setRoom(room);
        booking.setReviewed(false);
        booking.setPromotion(promotion);

        // Gán các khoản phí vào entity
        booking.setDiscount(priceCalculation.getDiscountAmount());
        booking.setTaxRate(priceCalculation.getTaxAmount());
        booking.setServiceFeeRate(priceCalculation.getServiceFee());

        // Handle payment policy
        if (room.getHotel().getPaymentPolicy() == PaymentPolicy.CHECKOUT) {
            booking.setStatus(BookingStatus.PAY_ON_CHECKOUT);
        } else {
            booking.setStatus(BookingStatus.PENDING);
        }

        Booking savedBooking = bookingRepository.save(booking);
        return convertToDto(savedBooking);
    }

    public List<BookingDto> getBookingHistory(String userEmail){
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng!"));
        // Lấy danh sách booking theo userId
        List<Booking> bookings = bookingRepository.findByUserId(user.getId());

        // Chuyển đổi sang DTO
        return bookings.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<BookingDto> getHotelBookingHistory(String userEmail) {
        // Lấy user từ email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng!"));

        // Lấy khách sạn thuộc sở hữu của user
        Optional<Hotel> ownedHotel = hotelRepository.findByOwnerId(user.getId());

        // Kiểm tra nếu ownedHotel tồn tại
        if (ownedHotel.isPresent()) {
            // Lấy tất cả các booking từ các phòng thuộc khách sạn của user và sắp xếp giảm dần theo thời gian tạo và thời gian trả phòng
            List<Booking> bookings = ownedHotel.get().getRooms().stream()
                    .flatMap(room -> room.getBookings().stream())
                    .sorted(Comparator
                            .comparing((Booking booking) -> booking.getCheckoutDate() != null ? booking.getCheckoutDate() : LocalDateTime.MIN)
                            .reversed()
                            .thenComparing(Comparator.comparing((Booking booking) -> booking.getCreatedDate() != null ? booking.getCreatedDate() : LocalDateTime.MIN).reversed()))
                    .collect(Collectors.toList());


            // Chuyển đổi danh sách bookings thành BookingDto
            return bookings.stream().map(this::convertToDto).collect(Collectors.toList());
        } else {
            // Trường hợp không tìm thấy khách sạn nào, trả về danh sách rỗng hoặc xử lý lỗi tùy theo yêu cầu
            throw new ResourceNotFoundException("Không tìm thấy khách sạn cho người dùng này!");
        }
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
    private PriceCalculationResult calculateTotalPrice(BigDecimal basePrice, LocalDateTime checkinDate,
                                                       LocalDateTime checkoutDate,
                                                       BigDecimal taxRate, BigDecimal serviceFeeRate, Promotion promotion) {
        // Tính số đêm lưu trú
        long numberOfNights = ChronoUnit.DAYS.between(checkinDate.toLocalDate(), checkoutDate.toLocalDate());
        if (numberOfNights <= 0) {
            return new PriceCalculationResult(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        // Tính giá phòng cơ bản cho toàn bộ số đêm
        BigDecimal roomCost = basePrice.multiply(BigDecimal.valueOf(numberOfNights));

        // Áp dụng mã giảm giá từ Promotion (nếu có)
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (promotion != null) {
            if (promotion.getDiscountType() == DiscountType.PERCENT) {
                discountAmount = roomCost.multiply(BigDecimal.valueOf(promotion.getValue()).divide(BigDecimal.valueOf(100)));
            } else if (promotion.getDiscountType() == DiscountType.FIXED) {
                discountAmount = BigDecimal.valueOf(promotion.getValue());
            }

            // Giới hạn giảm giá nếu vượt quá maxDiscountValue
            if (promotion.getMaxDiscountValue() != null && discountAmount.compareTo(promotion.getMaxDiscountValue()) > 0) {
                discountAmount = promotion.getMaxDiscountValue(); // Chỉ áp dụng đến maxDiscountValue
            }
        }

        // Tính giá sau khi giảm giá
        BigDecimal discountedPrice = roomCost.subtract(discountAmount);

        // Tính thuế và phí dịch vụ
        BigDecimal taxAmount = discountedPrice.multiply(taxRate != null ? taxRate : BigDecimal.ZERO);
        BigDecimal serviceFee = discountedPrice.multiply(serviceFeeRate != null ? serviceFeeRate : BigDecimal.ZERO);

        // Tính tổng giá cuối cùng
        BigDecimal totalPrice = discountedPrice.add(taxAmount).add(serviceFee);

        return new PriceCalculationResult(totalPrice, discountAmount, taxAmount, serviceFee);
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
        bookingDto.setCreatedDate(booking.getCreatedDate());
        bookingDto.setUpdatedDate(booking.getUpdatedDate());

        bookingDto.setBookingName(booking.getBookingName());
        bookingDto.setBookingPhone(booking.getBookingPhone());
        bookingDto.setBookingEmail(booking.getBookingEmail());
        bookingDto.setBookingNotes(booking.getBookingNotes());

        bookingDto.setStatus(booking.getStatus());
        bookingDto.setReviewed(booking.isReviewed());

        bookingDto.setBaseRatePerNight(booking.getBaseRatePerNight());
        bookingDto.setDiscount(booking.getDiscount());
        bookingDto.setTaxRate(booking.getTaxRate());
        bookingDto.setServiceFeeRate(booking.getServiceFeeRate());
        bookingDto.setTotalPrice(booking.getTotalPrice());

        // Thiết lập thông tin Promotion nếu có
        if (booking.getPromotion() != null) {
            bookingDto.setPromotionId(booking.getPromotion().getId());
        }

        // Thiết lập thông tin thanh toán
        if (booking.getPayment() != null) {
            bookingDto.setPaymentType(booking.getPayment().getPaymentType());
            bookingDto.setPayment(booking.getPayment());
        }

        // Thiết lập thông tin khóa ngoại
        if (booking.getUser() != null) {
            bookingDto.setUserId(booking.getUser().getId());
        }
        if (booking.getRoom() != null) {
            bookingDto.setRoomId(booking.getRoom().getId());
        }

        return bookingDto;
    }


    private Booking convertToEntity(BookingDto bookingDto) {
        Booking booking = new Booking();

        booking.setCheckinDate(bookingDto.getCheckinDate());
        booking.setCheckoutDate(bookingDto.getCheckoutDate());
        booking.setCreatedDate(bookingDto.getCreatedDate() != null ? bookingDto.getCreatedDate() : LocalDateTime.now());
        booking.setUpdatedDate(bookingDto.getUpdatedDate() != null ? bookingDto.getUpdatedDate() : LocalDateTime.now());

        booking.setBookingName(bookingDto.getBookingName());
        booking.setBookingPhone(bookingDto.getBookingPhone());
        booking.setBookingEmail(bookingDto.getBookingEmail());
        booking.setBookingNotes(bookingDto.getBookingNotes());

        booking.setStatus(bookingDto.getStatus() != null ? bookingDto.getStatus() : BookingStatus.PENDING);
        booking.setReviewed(bookingDto.isReviewed());

        booking.setBaseRatePerNight(bookingDto.getBaseRatePerNight());
        booking.setDiscount(bookingDto.getDiscount());
        booking.setTaxRate(bookingDto.getTaxRate());
        booking.setServiceFeeRate(bookingDto.getServiceFeeRate());

        booking.setTotalPrice(bookingDto.getTotalPrice());

        // Gán đối tượng Promotion nếu có promotionId
        if (bookingDto.getPromotionId() != null) {
            Promotion promotion = promotionRepository.findById(bookingDto.getPromotionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with ID: " + bookingDto.getPromotionId()));
            booking.setPromotion(promotion);
        }

        // Thiết lập đối tượng Payment nếu chưa có
        if (bookingDto.getPayment() == null) {
            Payment payment = new Payment();
            payment.setPaymentDate(LocalDateTime.now());
            payment.setAmount(bookingDto.getTotalPrice());
            payment.setPaymentType(bookingDto.getPaymentType());
            payment.setStatus(PaymentStatus.PENDING);
            payment.setBooking(booking); // Associate the payment with the booking
            booking.setPayment(payment);
        } else {
            booking.setPayment(bookingDto.getPayment());
        }

        return booking;
    }



}
