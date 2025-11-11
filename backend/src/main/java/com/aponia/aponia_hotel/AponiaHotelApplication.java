
package com.aponia.aponia_hotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.aponia.aponia_hotel.security.jwt.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class AponiaHotelApplication {
	public static void main(String[] args) {
		SpringApplication.run(AponiaHotelApplication.class, args);
	}

}
