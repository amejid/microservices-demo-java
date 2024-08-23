package com.microservices.demo.reactive.elastic.query.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class WebSecurityConfig {

	@Bean
	public SecurityWebFilterChain webFluxSecurityConfig(ServerHttpSecurity httpSecurity) {
		httpSecurity.authorizeExchange(authorize -> authorize.anyExchange().permitAll())
			.csrf(ServerHttpSecurity.CsrfSpec::disable);
		return httpSecurity.build();
	}

}
