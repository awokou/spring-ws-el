package com.server.spring.ws.el;

import com.server.spring.ws.el.service.BitPayService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringWsElApplication {

	private final BitPayService bitPayService;

    public SpringWsElApplication(BitPayService bitPayService) {
        this.bitPayService = bitPayService;
    }

    public static void main(String[] args) {
		SpringApplication.run(SpringWsElApplication.class, args);
	}

	@Bean
	public CommandLineRunner run() {
		return args -> bitPayService.fetchAndSaveBitPay();
	}
}
