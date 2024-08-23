package com.microservices.demo.elastic.query.service.business.impl;

import java.util.List;

import com.microservices.demo.config.ElasticQueryServiceConfigData;
import com.microservices.demo.elastic.model.index.impl.TwitterIndexModel;
import com.microservices.demo.elastic.query.client.service.ElasticQueryClient;
import com.microservices.demo.elastic.query.service.QueryType;
import com.microservices.demo.elastic.query.service.business.ElasticQueryService;
import com.microservices.demo.elastic.query.service.common.exception.ElasticQueryServiceException;
import com.microservices.demo.elastic.query.service.common.model.ElasticQueryServiceResponseModel;
import com.microservices.demo.elastic.query.service.model.ElasticQueryServiceAnalyticsResponseModel;
import com.microservices.demo.elastic.query.service.model.ElasticQueryServiceWordCountResponseModel;
import com.microservices.demo.elastic.query.service.model.assembler.ElasticQueryServiceResponseModelAssembler;
import com.microservices.demo.mdc.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class TwitterElasticQueryService implements ElasticQueryService {

	private static final Logger LOG = LoggerFactory.getLogger(TwitterElasticQueryService.class);

	private final ElasticQueryServiceResponseModelAssembler elasticQueryServiceResponseModelAssembler;

	private final ElasticQueryClient<TwitterIndexModel> elasticQueryClient;

	private final ElasticQueryServiceConfigData elasticQueryServiceConfigData;

	private final WebClient.Builder webClientBuilder;

	public TwitterElasticQueryService(
			ElasticQueryServiceResponseModelAssembler elasticQueryServiceResponseModelAssembler,
			ElasticQueryClient<TwitterIndexModel> elasticQueryClient,
			ElasticQueryServiceConfigData elasticQueryServiceConfigData,
			@Qualifier("webClientBuilder") WebClient.Builder webClientBuilder) {
		this.elasticQueryServiceResponseModelAssembler = elasticQueryServiceResponseModelAssembler;
		this.elasticQueryClient = elasticQueryClient;
		this.elasticQueryServiceConfigData = elasticQueryServiceConfigData;
		this.webClientBuilder = webClientBuilder;
	}

	@Override
	public ElasticQueryServiceResponseModel getDocumentById(String id) {
		LOG.info("Querying elasticsearch for document with id {}", id);
		return this.elasticQueryServiceResponseModelAssembler.toModel(this.elasticQueryClient.getIndexModelById(id));
	}

	@Override
	public ElasticQueryServiceAnalyticsResponseModel getDocumentsByText(String text, String accessToken) {
		LOG.info("Querying elasticsearch for documents with text {}", text);
		List<ElasticQueryServiceResponseModel> models = this.elasticQueryServiceResponseModelAssembler
			.toModels(this.elasticQueryClient.getIndexModelByText(text));
		return ElasticQueryServiceAnalyticsResponseModel.builder()
			.queryResponseModels(models)
			.wordCount(getWordCount(text, accessToken))
			.build();
	}

	@Override
	public List<ElasticQueryServiceResponseModel> getAllDocuments() {
		LOG.info("Querying elasticsearch for all documents");
		return this.elasticQueryServiceResponseModelAssembler.toModels(this.elasticQueryClient.getAllIndexModels());
	}

	private Long getWordCount(String text, String accessToken) {

		if (QueryType.KAFKA_STATE_STORE.getType()
			.equals(this.elasticQueryServiceConfigData.getWebClient().getQueryType())) {
			return getFromKafkaStateStore(text, accessToken).getWordCount();
		}
		else if (QueryType.ANALYTICS_DATABASE.getType()
			.equals(this.elasticQueryServiceConfigData.getWebClient().getQueryType())) {
			return getFromAnalyticsDatabase(text, accessToken).getWordCount();
		}
		return 0L;
	}

	private ElasticQueryServiceWordCountResponseModel getFromAnalyticsDatabase(String text, String accessToken) {
		ElasticQueryServiceConfigData.Query queryFromAnalyticsDatabase = this.elasticQueryServiceConfigData
			.getQueryFromAnalyticsDatabase();

		return retrieveResponseModel(text, accessToken, queryFromAnalyticsDatabase);
	}

	private ElasticQueryServiceWordCountResponseModel getFromKafkaStateStore(String text, String accessToken) {
		ElasticQueryServiceConfigData.Query queryFromKafkaStateStore = this.elasticQueryServiceConfigData
			.getQueryFromKafkaStateStore();

		return retrieveResponseModel(text, accessToken, queryFromKafkaStateStore);

	}

	private ElasticQueryServiceWordCountResponseModel retrieveResponseModel(String text, String accessToken,
			ElasticQueryServiceConfigData.Query queryFromKafkaStateStore) {
		return this.webClientBuilder.build()
			.method(HttpMethod.valueOf(queryFromKafkaStateStore.getMethod()))
			.uri(queryFromKafkaStateStore.getUri(), uriBuilder -> uriBuilder.build(text))
			.headers(httpHeaders -> {
				httpHeaders.setBearerAuth(accessToken);
				httpHeaders.set(Constants.CORRELATION_ID_HEADER, MDC.get(Constants.CORRELATION_ID_KEY));
			})
			.accept(MediaType.valueOf(queryFromKafkaStateStore.getAccept()))
			.retrieve()
			.onStatus(httpStatus -> httpStatus.equals(HttpStatus.UNAUTHORIZED),
					clientResponse -> Mono.just(new BadCredentialsException("Not authorized")))
			.onStatus(HttpStatus::is4xxClientError,
					clientResponse -> Mono
						.just(new ElasticQueryServiceException(clientResponse.statusCode().getReasonPhrase())))
			.onStatus(HttpStatus::is5xxServerError,
					clientResponse -> Mono.just(new Exception(clientResponse.statusCode().getReasonPhrase())))
			.bodyToMono(ElasticQueryServiceWordCountResponseModel.class)
			.log()
			.block();
	}

}
