package com.example.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProxyApplication.class, args);
	}

	@Bean
	RouteLocator myRouteLocator(RouteLocatorBuilder builder) {
		return builder.routes()
				.route("httpbin_route",
						route -> route
								.path("/httpbin/**")
								.filters(f -> f.stripPrefix(1))
								.uri("http://httpbin.org")
				).build();
	}
}
