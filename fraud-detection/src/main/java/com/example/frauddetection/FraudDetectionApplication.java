package com.example.frauddetection;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class FraudDetectionApplication {

	private static final Logger log = LoggerFactory.getLogger(FraudDetectionApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(FraudDetectionApplication.class, args);
	}

	@Bean
	// frauds
	// in
	// 0
	// frauds-in-0
	Consumer<String> frauds() {
		return s -> log.info("Got a message with body [" + s + "]");
	}
}

@RestController
class FraudDetectionController {

	private static final Logger log = LoggerFactory.getLogger(FraudDetectionController.class);

	@GetMapping("/frauds")
	List<String> frauds() {
		log.info("\n\nGot fraud request\n\n");
		return Arrays.asList("josh", "marcin");
	}
}