package com.backend.KKUN_Booking;


import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class KkunBookingApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().filename("local.env").load();
		SpringApplication.run(KkunBookingApplication.class, args);
	}

}
