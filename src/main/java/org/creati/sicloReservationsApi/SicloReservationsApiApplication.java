package org.creati.sicloReservationsApi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SicloReservationsApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SicloReservationsApiApplication.class, args);
	}

}
