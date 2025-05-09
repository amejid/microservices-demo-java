package com.microservices.demo.reactive.elastic.query.service.api;

import com.microservices.demo.elastic.query.service.common.model.ElasticQueryServiceRequestModel;
import com.microservices.demo.elastic.query.service.common.model.ElasticQueryServiceResponseModel;
import com.microservices.demo.reactive.elastic.query.service.business.ElasticQueryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/documents")
public class ElasticDocumentController {

	private static final Logger LOG = LoggerFactory.getLogger(ElasticDocumentController.class);

	private final ElasticQueryService elasticQueryService;

	public ElasticDocumentController(ElasticQueryService elasticQueryService) {
		this.elasticQueryService = elasticQueryService;
	}

	@PostMapping(value = "/get-doc-by-text", produces = MediaType.TEXT_EVENT_STREAM_VALUE,
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public Flux<ElasticQueryServiceResponseModel> getDocumentByText(
			@Valid @RequestBody ElasticQueryServiceRequestModel requestModel) {
		Flux<ElasticQueryServiceResponseModel> response = this.elasticQueryService
			.getDocumentByText(requestModel.getText());
		response = response.log();
		LOG.info("Returning from query reactive service for text: {}", requestModel.getText());
		return response;
	}

}
