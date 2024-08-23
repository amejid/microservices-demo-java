package com.microservices.demo.elastic.query.service.security;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import com.microservices.demo.elastic.query.service.common.model.ElasticQueryServiceRequestModel;
import com.microservices.demo.elastic.query.service.common.model.ElasticQueryServiceResponseModel;
import com.microservices.demo.elastic.query.service.model.ElasticQueryServiceAnalyticsResponseModel;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class QueryServicePermissionEvaluator implements PermissionEvaluator {

	private static final String SUPER_USER_ROLE = "APP_SUPER_USER_ROLE";

	private final HttpServletRequest httpServletRequest;

	public QueryServicePermissionEvaluator(HttpServletRequest httpServletRequest) {
		this.httpServletRequest = httpServletRequest;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomain, Object permission) {
		if (isSuperUser()) {
			return true;
		}
		if (targetDomain instanceof ElasticQueryServiceRequestModel elasticQueryServiceRequestModel) {
			return preAuthorize(authentication, elasticQueryServiceRequestModel.getId(), permission);
		}
		else if (targetDomain instanceof ResponseEntity || targetDomain == null) {
			if (targetDomain == null) {
				return true;
			}
			ElasticQueryServiceAnalyticsResponseModel responseBody = ((ResponseEntity<ElasticQueryServiceAnalyticsResponseModel>) targetDomain)
				.getBody();
			Objects.requireNonNull(responseBody);
			return postAuthorize(authentication, responseBody.getQueryResponseModels(), permission);
		}
		return false;
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
			Object permission) {
		if (isSuperUser()) {
			return true;
		}
		if (targetId == null) {
			return false;
		}
		return preAuthorize(authentication, (String) targetId, permission);
	}

	private boolean postAuthorize(Authentication authentication, List<ElasticQueryServiceResponseModel> responseBody,
			Object permission) {
		TwitterQueryUser twitterQueryUser = (TwitterQueryUser) authentication.getPrincipal();
		for (ElasticQueryServiceResponseModel responseModel : responseBody) {
			PermissionType userPermission = twitterQueryUser.getPermissions().get(responseModel.getId());
			if (!hasPermission(permission, userPermission)) {
				return false;
			}
		}
		return true;
	}

	private boolean preAuthorize(Authentication authentication, String id, Object permission) {
		TwitterQueryUser twitterQueryUser = (TwitterQueryUser) authentication.getPrincipal();
		PermissionType userPermission = twitterQueryUser.getPermissions().get(id);
		return hasPermission(permission, userPermission);
	}

	private boolean hasPermission(Object permission, PermissionType userPermission) {
		return userPermission != null && permission.equals(userPermission.getType());
	}

	private boolean isSuperUser() {
		return this.httpServletRequest.isUserInRole(SUPER_USER_ROLE);
	}

}
