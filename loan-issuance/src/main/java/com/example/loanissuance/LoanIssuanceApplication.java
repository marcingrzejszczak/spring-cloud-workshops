package com.example.loanissuance;

import java.util.Arrays;
import java.util.List;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableFeignClients
public class LoanIssuanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoanIssuanceApplication.class, args);
	}

	@Bean
	@Primary
	RestTemplate nonLoadBalancedRestTemplate() {
		return new RestTemplate();
	}

	@Bean
	@LoadBalanced
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
		return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
				.circuitBreakerConfig(CircuitBreakerConfig.custom()
						.minimumNumberOfCalls(5).build())
				.build());
	}
}

@RestController
class LoanIssuanceController {

	private static final Logger log = LoggerFactory.getLogger(LoanIssuanceController.class);

	private final RestTemplate restTemplate;

	private final FraudClient fraudClient;

	private final CircuitBreakerFactory factory;

	private final StreamBridge bridge;

	LoanIssuanceController(@LoadBalanced RestTemplate restTemplate, FraudClient fraudClient, CircuitBreakerFactory factory, StreamBridge bridge) {
		this.restTemplate = restTemplate;
		this.fraudClient = fraudClient;
		this.factory = factory;
		this.bridge = bridge;
	}

	@PostMapping("/stream")
	void endpointPresent(@RequestBody String body) {
		// reuse spring cloud stream
		this.bridge.send("frauds-out-0", body);
	}


	@GetMapping("/resttemplate/loan/{id}/fraud")
	@SuppressWarnings("unchecked")
	List<String> frauds(@PathVariable("id") int id) {
		log.info("\n\nGot loan/" + id + "/fraud request\n\n");
		return this.factory.create("endpoint_present")
				.run(() -> this.restTemplate.getForObject("http://fraud-detection/frauds", List.class));
	}

	@GetMapping("/missing")
	@SuppressWarnings("unchecked")
	List<String> endpointMissing() {
		return this.factory.create("endpoint_missing").run(() ->
		{
			try {
				Thread.sleep(500);
				return new RestTemplate().getForObject("http://localhost:9080/missing", List.class);
			}
			catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
		});
	}

	@GetMapping("/fallback")
	List<String> fallback() {
		return this.factory.create("endpoint_fallback").run(this::endpointMissing,
				throwable -> Arrays.asList("short", "circuit"));
	}

	@GetMapping("/openfeign/loan/{id}/fraud")
	@SuppressWarnings("unchecked")
	List<String> openFeignFrauds(@PathVariable("id") int id) {
		System.out.println("\n\nFeign: Got loan/" + id + "/fraud request\n\n");
		return this.fraudClient.frauds();
	}

	@GetMapping("/proxy/fraud")
	@SuppressWarnings("unchecked")
	List<String> proxyFrauds() {
		log.info("\n\nGot /proxy/fraud request\n\n");
		return this.restTemplate.getForObject("http://proxy/fraud-detection/frauds", List.class);
	}
}

@FeignClient("fraud-detection")
interface FraudClient {

	@GetMapping("/frauds")
	List<String> frauds();
}