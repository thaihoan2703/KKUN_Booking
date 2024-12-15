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
            public String amenity; // Loại tiện ích (ví dụ: restaurant)
            public String tourism; // Địa điểm du lịch (ví dụ: hotel, museum)
            public String leisure; // Tiện ích giải trí (ví dụ: park, stadium)
            public String historic; // Di tích lịch sử (ví dụ: monument, ruins)
            public String natural; // Đặc điểm tự nhiên (ví dụ: beach, cliff)
            public String place; // Địa danh (ví dụ: square, locality)
            public String healthcare; // Địa danh (ví dụ: benh vien)

            public String check_date;
            public String contact_facebook;
            public String contact_instagram;
            public String contact_twitter;
            public String cuisine;
            public String currency;
            public String name;
            public String name_en;
            public String opening_hours;
            public String payment_lightning;
            public String payment_lightning_contactless;
            public String payment_onchain;
            public String phone;
            public String website;
            public String addr_city;
            public String addr_district;
            public String addr_housenumber;
            public String addr_street;
            public String addr_subdistrict;
        }
    }
}
