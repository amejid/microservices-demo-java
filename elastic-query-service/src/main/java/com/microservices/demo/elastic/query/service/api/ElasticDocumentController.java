package com.microservices.demo.elastic.query.service.api;

import java.util.List;

import com.microservices.demo.elastic.query.service.business.ElasticQueryService;
import com.microservices.demo.elastic.query.service.common.model.ElasticQueryServiceRequestModel;
import com.microservices.demo.elastic.query.service.common.model.ElasticQueryServiceResponseModel;
import com.microservices.demo.elastic.query.service.model.ElasticQueryServiceAnalyticsResponseModel;
import com.microservices.demo.elastic.query.service.security.TwitterQueryUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping(value = "/documents", produces = "application/vnd.api.v1+json")
public class ElasticDocumentController {

	private static final Logger LOG = LoggerFactory.getLogger(ElasticDocumentController.class);

	private final ElasticQueryService elasticQueryService;

	public ElasticDocumentController(ElasticQueryService elasticQueryService) {
		this.elasticQueryService = elasticQueryService;
	}

	@PostAuthorize("hasPermission(returnObject, 'READ')")
	@Operation(summary = "Get all elastic documents")
	@ApiResponse(responseCode = "200", description = "Success",
			content = { @Content(mediaType = "application/vnd.api.v1+json",
					array = @ArraySchema(schema = @Schema(implementation = ElasticQueryServiceResponseModel.class))) })
	@ApiResponse(responseCode = "400", description = "Not found")
	@ApiResponse(responseCode = "500", description = "Internal server error")
	@GetMapping
	public ResponseEntity<List<ElasticQueryServiceResponseModel>> getAllDocuments() {
		List<ElasticQueryServiceResponseModel> response = this.elasticQueryService.getAllDocuments();
		LOG.info("Elasticsearch returned {} documents", response.size());
		return ResponseEntity.ok(response);
	}

	@PreAuthorize("hasPermission(#id, 'ElasticQueryServiceResponseModel','READ')")
	@Operation(summary = "Get elastic document by id")
	@ApiResponse(responseCode = "200", description = "Success",
			content = { @Content(mediaType = "application/vnd.api.v1+json",
					schema = @Schema(implementation = ElasticQueryServiceResponseModel.class)) })
	@ApiResponse(responseCode = "400", description = "Not found")
	@ApiResponse(responseCode = "500", description = "Internal server error")
	@GetMapping("/{id}")
	public ResponseEntity<ElasticQueryServiceResponseModel> getDocumentById(@NotEmpty @PathVariable String id) {
		ElasticQueryServiceResponseModel response = this.elasticQueryService.getDocumentById(id);
		LOG.info("Elasticsearch returned document with id {}", id);
		return ResponseEntity.ok(response);
	}

	@PreAuthorize("hasRole('APP_USER_ROLE') || hasRole('APP_SUPER_USER_ROLE') || hasAuthority('SCOPE_APP_USER_ROLE')")
	@PostAuthorize("hasPermission(returnObject, 'READ')")
	@Operation(summary = "Get elastic document by text")
	@ApiResponse(responseCode = "200", description = "Success",
			content = { @Content(mediaType = "application/vnd.api.v1+json",
					array = @ArraySchema(schema = @Schema(implementation = ElasticQueryServiceResponseModel.class))) })
	@ApiResponse(responseCode = "400", description = "Not found")
	@ApiResponse(responseCode = "500", description = "Internal server error")
	@PostMapping("/get-document-by-text")
	public ResponseEntity<ElasticQueryServiceAnalyticsResponseModel> getDocumentByText(
			@Valid @RequestBody ElasticQueryServiceRequestModel request,
			@AuthenticationPrincipal TwitterQueryUser principal,
			@RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient oAuth2AuthorizedClient) {
		ElasticQueryServiceAnalyticsResponseModel response = this.elasticQueryService
			.getDocumentsByText(request.getText(), oAuth2AuthorizedClient.getAccessToken().getTokenValue());
		LOG.info("Elasticsearch returned {} documents", response.getQueryResponseModels().size());
		return ResponseEntity.ok(response);
	}

}
