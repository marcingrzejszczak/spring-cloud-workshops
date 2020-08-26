package com.example.configclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableConfigurationProperties(MyProps.class)
public class ConfigClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigClientApplication.class, args);
	}

}

@RestController
@RefreshScope
class ConfigClientController {

	private final String value;

	ConfigClientController(@Value("${foo:test}") String value) {
		this.value = value;
	}

	@GetMapping("/foo")
	String foo() {
		return this.value;
	}
}

@RestController
class BonusController {

	private final String value;

	BonusController(@Value("${bonus:test}") String value) {
		this.value = value;
	}

	@GetMapping("/bonus")
	String foo() {
		return this.value;
	}
}

//remember about @EnableConfigurationProperties
@RestController
class ConfigPropsController {

	private final MyProps value;

	ConfigPropsController(MyProps value) {
		this.value = value;
	}

	@GetMapping("/props")
	String foo() {
		return this.value.getMessage();
	}
}

@ConfigurationProperties("democonfigclient")
class MyProps {
	private String message;

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}