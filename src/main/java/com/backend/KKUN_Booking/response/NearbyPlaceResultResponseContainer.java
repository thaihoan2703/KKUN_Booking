package com.backend.KKUN_Booking.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class NearbyPlaceResultResponseContainer {
    public List<NearbyPlaceResultResponse> elements;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class NearbyPlaceResultResponse {
        public String type; // Loại đối tượng (ví dụ: node)
        public long id; // ID của đối tượng
        public double lat; // Vĩ độ
        public double lon; // Kinh độ
        public NearbyPlaceResultResponseContainer.NearbyPlaceResultResponse.Tags tags; // Thông tin bổ sung

        @JsonIgnoreProperties(ignoreUnknown = true)
        @Data
        public static class Tags {
            public String amenity; // Loại địa điểm (ví dụ: restaurant)
            public String check_date; // Ngày kiểm tra
            public String contact_facebook; // Liên kết Facebook
            public String contact_instagram; // Liên kết Instagram
            public String contact_twitter; // Liên kết Twitter
            public String cuisine; // Ẩm thực
            public String currency; // Loại tiền tệ
            public String name; // Tên địa điểm
            public String name_en; // Tên địa điểm Tieng Anh
            public String tourism; // diem nhan
            public String opening_hours; // Giờ mở cửa
            public String payment_lightning; // Thanh toán bằng Lightning
            public String payment_lightning_contactless; // Thanh toán không tiếp xúc
            public String payment_onchain; // Thanh toán On-chain
            public String phone; // Số điện thoại
            public String website; // Liên kết trang web
            public String addr_city; // Tên thành phố
            public String addr_district; // Quận
            public String addr_housenumber; // Số nhà
            public String addr_street; // Đường
            public String addr_subdistrict; // Phường
        }
    }
}
