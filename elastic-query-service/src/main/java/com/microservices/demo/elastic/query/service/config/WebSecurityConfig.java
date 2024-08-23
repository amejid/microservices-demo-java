package com.microservices.demo.elastic.query.service.config;

import java.util.Arrays;

import com.microservices.demo.elastic.query.service.security.QueryServicePermissionEvaluator;
import com.microservices.demo.elastic.query.service.security.TwitterQueryUserDetailsService;
import com.microservices.demo.elastic.query.service.security.TwitterQueryUserJwtConverter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

	private final TwitterQueryUserDetailsService twitterQueryUserDetailsService;

	private final OAuth2ResourceServerProperties oAuth2ResourceServerProperties;

	private final QueryServicePermissionEvaluator queryServicePermissionEvaluator;

	public WebSecurityConfig(TwitterQueryUserDetailsService twitterQueryUserDetailsService,
			OAuth2ResourceServerProperties oAuth2ResourceServerProperties,
			QueryServicePermissionEvaluator queryServicePermissionEvaluator) {

		this.twitterQueryUserDetailsService = twitterQueryUserDetailsService;
		this.oAuth2ResourceServerProperties = oAuth2ResourceServerProperties;
		this.queryServicePermissionEvaluator = queryServicePermissionEvaluator;
	}

	@Value("${security.paths-to-ignore}")
	private String[] pathsToIgnore;

	@Bean
	public MethodSecurityExpressionHandler expressionHandler() {
		DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
		expressionHandler.setPermissionEvaluator(this.queryServicePermissionEvaluator);
		return expressionHandler;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(requests -> requests
			.requestMatchers(Arrays.stream(this.pathsToIgnore)
				.map(AntPathRequestMatcher::new)
				.toList()
				.toArray(new RequestMatcher[] {}))
			.permitAll()
			.anyRequest()
			.authenticated())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.csrf(AbstractHttpConfigurer::disable)
			.oauth2ResourceServer(
					oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(twitterQueryUserJwtConverter())));

		return http.build();
	}

	@Bean
	JwtDecoder jwtDecoder(
			@Qualifier("elasticQueryServiceAudienceValidator") OAuth2TokenValidator<Jwt> audienceValidator) {
		NimbusJwtDecoder jwtDecoder = JwtDecoders
			.fromOidcIssuerLocation(this.oAuth2ResourceServerProperties.getJwt().getIssuerUri());
		OAuth2TokenValidator<Jwt> withIssuer = JwtValidators
			.createDefaultWithIssuer(this.oAuth2ResourceServerProperties.getJwt().getIssuerUri());
		OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);
		jwtDecoder.setJwtValidator(withAudience);
		return jwtDecoder;
	}

	@Bean
	Converter<Jwt, AbstractAuthenticationToken> twitterQueryUserJwtConverter() {
		return new TwitterQueryUserJwtConverter(this.twitterQueryUserDetailsService);
	}

	@Bean
	protected PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
