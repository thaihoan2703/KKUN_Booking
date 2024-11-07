package com.backend.KKUN_Booking.service;

import com.backend.KKUN_Booking.dto.BookingDto;
import com.backend.KKUN_Booking.model.Booking;
import com.backend.KKUN_Booking.model.enumModel.BookingStatus;
import com.backend.KKUN_Booking.model.enumModel.PaymentType;
import com.backend.KKUN_Booking.response.PaymentResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.UUID;

public interface BookingService {
    List<BookingDto> getAllBookings();
    BookingDto getBookingById(UUID id);
    BookingDto createBooking(BookingDto bookingDto, String userEmail);
    BookingDto updateBooking(UUID id, BookingDto bookingDto, String userEmail);
    void deleteBooking(UUID id);

    PaymentResponse.BaseResponse initiatePayment(UUID bookingId, PaymentType paymentType, HttpServletRequest request);
    void updateBookingStatus(UUID bookingId, BookingStatus status);
    void markBookingAsReviewed(UUID bookingId);
    List<Booking> getCompletedBookingsWithoutReview();
    List<BookingDto> getBookingHistory(String userEmail);
    List<BookingDto> getHotelBookingHistory(String userEmail);

}

