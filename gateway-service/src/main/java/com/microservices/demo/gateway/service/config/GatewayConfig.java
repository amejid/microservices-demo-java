package com.microservices.demo.gateway.service.config;

import java.time.Duration;
import java.util.Objects;

import com.microservices.demo.config.GatewayServiceConfigData;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import reactor.core.publisher.Mono;

import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

	private final GatewayServiceConfigData gatewayServiceConfigData;

	private static final String HEADER_FOR_KEY_RESOLVER = "Authorization";

	public GatewayConfig(GatewayServiceConfigData gatewayServiceConfigData) {
		this.gatewayServiceConfigData = gatewayServiceConfigData;
	}

	@Bean("authHeaderResolver")
	KeyResolver userKeyResolver() {
		return exchange -> Mono
			.just(Objects.requireNonNull(exchange.getRequest().getHeaders().getFirst(HEADER_FOR_KEY_RESOLVER)));
	}

	@Bean
	Customizer<ReactiveResilience4JCircuitBreakerFactory> circuitBreakerFactoryCustomizer() {
		return reactiveResilience4JCircuitBreakerFactory -> reactiveResilience4JCircuitBreakerFactory
			.configureDefault(id -> new Resilience4JConfigBuilder(id)
				.timeLimiterConfig(TimeLimiterConfig.custom()
					.timeoutDuration(Duration.ofMillis(this.gatewayServiceConfigData.getTimeoutMs()))
					.build())
				.circuitBreakerConfig(CircuitBreakerConfig.custom()
					.failureRateThreshold(this.gatewayServiceConfigData.getFailureRateThreshold())
					.slowCallRateThreshold(this.gatewayServiceConfigData.getSlowCallRateThreshold())
					.slowCallDurationThreshold(
							Duration.ofMillis(this.gatewayServiceConfigData.getSlowCallDurationThreshold()))
					.permittedNumberOfCallsInHalfOpenState(
							this.gatewayServiceConfigData.getPermittedNumOfCallsInHalfOpenState())
					.slidingWindowSize(this.gatewayServiceConfigData.getSlidingWindowSize())
					.minimumNumberOfCalls(this.gatewayServiceConfigData.getMinNumberOfCalls())
					.waitDurationInOpenState(
							Duration.ofMillis(this.gatewayServiceConfigData.getWaitDurationInOpenState()))
					.build())
				.build());
	}

}
