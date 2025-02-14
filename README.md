# Hướng Dẫn Sử Dụng

## Giới thiệu
Đây là tài liệu hướng dẫn sử dụng dự án/file KKUN Booking. Dự án này giúp cải thiện các chức năng tìm kiếm, đặt phòng, kết hợp RASA Chatbot và sẽ được mở rộng thêm trong tương lai.

## Yêu cầu hệ thống
- Sử dụng ngôn ngữ Java.
- Các thư viện hoặc phần mềm cần thiết:
  - IDE InteliJ Idea (hoặc các IDE hỗ trợ Java)
  - Spring Boot
## Cài đặt
1. Clone dự án về máy:
   - git clone https://github.com/thaihoandev/KKUN_Booking.git
2. Tạo file local.env
   - Tạo file local.env trên dự án (nằm cạnh các file như: pom.xml, mvnw,...)
   - Nếu sử dụng InteliJ Idea
       - Vào Settings -> Plugins -> Tải EnvFile -> Restart IDE
       - Vào Run/Debug Configurations -> Bật Enable EnvFile -> Add file *.env (local.env) 
   - Cập nhật vào file *.env các Key đã lưu trong file application.properties
   - Tăng tính bảo mật
4. Sử dụng Database Postgres
5. Chạy dự án.
