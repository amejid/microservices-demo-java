package com.microservices.demo.gateway.service.controller;

import com.microservices.demo.gateway.service.model.AnalyticsDataFallbackModel;
import com.microservices.demo.gateway.service.model.QueryServiceFallbackModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

	private static final Logger LOG = LoggerFactory.getLogger(FallbackController.class);

	@PostMapping("/query-fallback")
	public ResponseEntity<QueryServiceFallbackModel> queryServiceFallback() {
		LOG.info("Returning fallback result for elastic-query-service");
		return ResponseEntity
			.ok(QueryServiceFallbackModel.builder().fallbackMessage("Fallback message from query service").build());
	}

	@PostMapping("/analytics-fallback")
	public ResponseEntity<AnalyticsDataFallbackModel> analyticsServiceFallback() {
		LOG.info("Returning fallback result for analytics-service");
		return ResponseEntity.ok(AnalyticsDataFallbackModel.builder().wordCount(0L).build());
	}

	@PostMapping("/streams-fallback")
	public ResponseEntity<AnalyticsDataFallbackModel> streamsServiceFallback() {
		LOG.info("Returning fallback result for kafka-streams-service");
		return ResponseEntity.ok(AnalyticsDataFallbackModel.builder().wordCount(0L).build());
	}

}
