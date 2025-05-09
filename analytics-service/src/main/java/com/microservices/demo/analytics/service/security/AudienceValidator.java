package com.microservices.demo.analytics.service.security;

import com.microservices.demo.config.AnalyticsServiceConfigData;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Qualifier("analyticsServiceAudienceValidator")
@Component
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

	private final AnalyticsServiceConfigData analyticsServiceConfigData;

	public AudienceValidator(AnalyticsServiceConfigData config) {
		this.analyticsServiceConfigData = config;
	}

	public OAuth2TokenValidatorResult validate(Jwt jwt) {
		if (jwt.getAudience().contains(this.analyticsServiceConfigData.getCustomAudience())) {
			return OAuth2TokenValidatorResult.success();
		}
		else {
			OAuth2Error audienceError = new OAuth2Error("invalid_token",
					"The required audience " + this.analyticsServiceConfigData.getCustomAudience() + " is missing!",
					null);
			return OAuth2TokenValidatorResult.failure(audienceError);
		}
	}

}
