package com.microservices.demo.reactive.elastic.query.web.client.service.impl;

import com.microservices.demo.config.ElasticQueryWebClientConfigData;
import com.microservices.demo.elastic.query.web.client.common.exception.ElasticQueryWebClientException;
import com.microservices.demo.elastic.query.web.client.common.model.ElasticQueryWebClientRequestModel;
import com.microservices.demo.elastic.query.web.client.common.model.ElasticQueryWebClientResponseModel;
import com.microservices.demo.reactive.elastic.query.web.client.service.ElasticQueryWebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class TwitterElasticQueryWebClient implements ElasticQueryWebClient {

	private static final Logger LOG = LoggerFactory.getLogger(TwitterElasticQueryWebClient.class);

	private final WebClient webClient;

	private final ElasticQueryWebClientConfigData elasticQueryWebClientConfigData;

	public TwitterElasticQueryWebClient(@Qualifier("webClient") WebClient webClient,
			ElasticQueryWebClientConfigData elasticQueryWebClientConfigData) {
		this.webClient = webClient;
		this.elasticQueryWebClientConfigData = elasticQueryWebClientConfigData;
	}

	@Override
	public Flux<ElasticQueryWebClientResponseModel> getDataByText(ElasticQueryWebClientRequestModel request) {
		LOG.info("Querying by text {}", request.getText());
		return getWebClient(request).bodyToFlux(ElasticQueryWebClientResponseModel.class);
	}

	private WebClient.ResponseSpec getWebClient(ElasticQueryWebClientRequestModel requestModel) {
		return this.webClient
			.method(HttpMethod.valueOf(this.elasticQueryWebClientConfigData.getQueryByText().getMethod()))
			.uri(this.elasticQueryWebClientConfigData.getQueryByText().getUri())
			.accept(MediaType.valueOf(this.elasticQueryWebClientConfigData.getQueryByText().getAccept()))
			.body(BodyInserters.fromPublisher(Mono.just(requestModel), createParametrizedTypeReference()))
			.retrieve()
			.onStatus(httpStatus -> httpStatus.equals(HttpStatus.UNAUTHORIZED),
					clientResponse -> Mono.just(new BadCredentialsException("Not authenticated!")))
			.onStatus(HttpStatus::is4xxClientError,
					clientResponse -> Mono
						.just(new ElasticQueryWebClientException(clientResponse.statusCode().getReasonPhrase())))
			.onStatus(HttpStatus::is5xxServerError,
					clientResponse -> Mono.just(new Exception(clientResponse.statusCode().getReasonPhrase())));
	}

	private <T> ParameterizedTypeReference<T> createParametrizedTypeReference() {
		return new ParameterizedTypeReference<>() {
		};
	}

}
