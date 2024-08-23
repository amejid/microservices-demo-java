package com.microservices.demo.analytics.service.api;

import java.util.Optional;

import com.microservices.demo.analytics.service.business.AnalyticsService;
import com.microservices.demo.analytics.service.model.AnalyticsResponseModel;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping(value = "/", produces = "application/vnd.api.v1+json")
public class AnalyticsController {

	private static final Logger LOG = LoggerFactory.getLogger(AnalyticsController.class);

	private final AnalyticsService analyticsService;

	public AnalyticsController(AnalyticsService analyticsService) {
		this.analyticsService = analyticsService;
	}

	@ApiResponse(responseCode = "200", description = "Success",
			content = { @Content(mediaType = "application/vnd.api.v1+json",
					schema = @Schema(implementation = AnalyticsResponseModel.class)) })
	@ApiResponse(responseCode = "400", description = "Not found")
	@ApiResponse(responseCode = "500", description = "Unexpected error")
	@GetMapping("/get-word-count-by-word/{word}")
	public ResponseEntity<AnalyticsResponseModel> getWordCountByWord(@PathVariable @NotEmpty String word) {
		Optional<AnalyticsResponseModel> response = this.analyticsService.getWordAnalytics(word);
		if (response.isPresent()) {
			LOG.info("Analytics data returned with id {}", response.get().getId());
			return ResponseEntity.ok(response.get());
		}
		return ResponseEntity.ok(AnalyticsResponseModel.builder().build());
	}

}
