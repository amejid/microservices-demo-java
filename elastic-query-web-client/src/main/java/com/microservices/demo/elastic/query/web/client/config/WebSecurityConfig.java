package com.microservices.demo.elastic.query.web.client.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.microservices.demo.config.RetryConfigData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class WebSecurityConfig {

	private static final Logger LOG = LoggerFactory.getLogger(WebSecurityConfig.class);

	private static final String ROLE_PREFIX = "ROLE_";

	private static final String GROUPS_CLAIM = "groups";

	@Value("${security.logout-success-url}")
	private String logoutSuccessUrl;

	private final ClientRegistrationRepository clientRegistrationRepository;

	public WebSecurityConfig(ClientRegistrationRepository clientRegistrationRepository) {
		this.clientRegistrationRepository = clientRegistrationRepository;
	}

	OidcClientInitiatedLogoutSuccessHandler oidcClientInitiatedLogoutSuccessHandler() {
		OidcClientInitiatedLogoutSuccessHandler successHandler = new OidcClientInitiatedLogoutSuccessHandler(
				this.clientRegistrationRepository);
		successHandler.setPostLogoutRedirectUri("{baseUrl}");
		return successHandler;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, RetryConfigData retryConfigData) throws Exception {
		http.authorizeHttpRequests(requests -> requests.requestMatchers(new AntPathRequestMatcher("/"))
			.permitAll()
			.anyRequest()
			.authenticated())
			.logout(conf -> conf.logoutSuccessHandler(oidcClientInitiatedLogoutSuccessHandler()))
			.oauth2Client(Customizer.withDefaults())
			.oauth2Login(conf -> conf.userInfoEndpoint(Customizer.withDefaults()));

		return http.build();
	}

	@Bean
	public GrantedAuthoritiesMapper userAuthoritiesMapper() {
		return authorities -> {
			Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

			authorities.forEach(authority -> {
				if (authority instanceof OidcUserAuthority oidcUserAuthority) {
					OidcIdToken oidcIdToken = oidcUserAuthority.getIdToken();
					LOG.info("Username from id token: {}", oidcIdToken.getPreferredUsername());
					OidcUserInfo oidcUserInfo = oidcUserAuthority.getUserInfo();

					List<SimpleGrantedAuthority> groupAuthorities = oidcUserInfo.getClaimAsStringList(GROUPS_CLAIM)
						.stream()
						.map(group -> new SimpleGrantedAuthority(ROLE_PREFIX + group.toUpperCase()))
						.toList();

					mappedAuthorities.addAll(groupAuthorities);
				}
			});
			return mappedAuthorities;
		};
	}

}
