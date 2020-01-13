package com.example.frauddetection;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableScheduling
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

	private final AtomicInteger gauge;

	private final Counter counter;

	FraudDetectionController(MeterRegistry meterRegistry) {
		this.gauge = meterRegistry.gauge("frauds_current", new AtomicInteger());
		this.counter = meterRegistry.counter("frauds_counter");
	}

	@GetMapping("/frauds")
	List<String> frauds() {
		log.info("\n\nGot fraud request\n\n");
		this.counter.increment();
		return Arrays.asList("josh", "marcin");
	}

	@GetMapping("/frauds/gauge")
	int countFraudsWithGauge() {
		return this.gauge.get();
	}

	@GetMapping("/frauds/counter")
	double countFraudsWithCounter() {
		return this.counter.count();
	}

	@Scheduled(fixedRate = 1L)
	void changeGaugeValue() {
		this.gauge.set(new Random().nextInt(100) + 200);
	}
}