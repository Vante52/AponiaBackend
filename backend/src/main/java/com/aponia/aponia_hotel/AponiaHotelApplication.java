
package com.aponia.aponia_hotel;

import com.aponia.aponia_hotel.config.ExternalApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ExternalApiProperties.class)
public class AponiaHotelApplication {

        public static void main(String[] args) {
                SpringApplication.run(AponiaHotelApplication.class, args);
        }

}
