package com.microservices.demo.kafka.streams.service.config;

import java.util.Arrays;

import com.microservices.demo.kafka.streams.service.security.KafkaStreamsUserDetailsService;
import com.microservices.demo.kafka.streams.service.security.KafkaStreamsUserJwtConverter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

	private final KafkaStreamsUserDetailsService kafkaStreamsUserDetailsService;

	private final OAuth2ResourceServerProperties oAuth2ResourceServerProperties;

	@Value("${security.paths-to-ignore}")
	private String[] pathsToIgnore;

	public WebSecurityConfig(KafkaStreamsUserDetailsService kafkaStreamsUserDetailsService,
			OAuth2ResourceServerProperties oAuth2ResourceServerProperties) {
		this.oAuth2ResourceServerProperties = oAuth2ResourceServerProperties;
		this.kafkaStreamsUserDetailsService = kafkaStreamsUserDetailsService;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(requests -> requests
			.requestMatchers(Arrays.stream(this.pathsToIgnore)
				.map(AntPathRequestMatcher::new)
				.toList()
				.toArray(new AntPathRequestMatcher[] {}))
			.permitAll()
			.anyRequest()
			.authenticated())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.csrf(AbstractHttpConfigurer::disable)
			.oauth2ResourceServer(
					oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(kafkaStreamsUserJwtAuthConverter())));
		return http.build();
	}

	@Bean
	JwtDecoder jwtDecoder(
			@Qualifier("kafkaStreamsServiceAudienceValidator") OAuth2TokenValidator<Jwt> audienceValidator) {
		NimbusJwtDecoder jwtDecoder = JwtDecoders
			.fromOidcIssuerLocation(this.oAuth2ResourceServerProperties.getJwt().getIssuerUri());
		OAuth2TokenValidator<Jwt> withIssuer = JwtValidators
			.createDefaultWithIssuer(this.oAuth2ResourceServerProperties.getJwt().getIssuerUri());
		OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);
		jwtDecoder.setJwtValidator(withAudience);
		return jwtDecoder;
	}

	@Bean
	Converter<Jwt, AbstractAuthenticationToken> kafkaStreamsUserJwtAuthConverter() {
		return new KafkaStreamsUserJwtConverter(this.kafkaStreamsUserDetailsService);
	}

}
