package com.microservices.demo.elastic.config;

import java.util.Objects;

import com.microservices.demo.config.ElasticConfigData;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.microservices.demo.elastic")
public class ElasticsearchConfig extends AbstractElasticsearchConfiguration {

	private final ElasticConfigData elasticConfigData;

	public ElasticsearchConfig(ElasticConfigData elasticConfigData) {
		this.elasticConfigData = elasticConfigData;
	}

	@Override
	@Bean
	public RestHighLevelClient elasticsearchClient() {
		UriComponents serverUri = UriComponentsBuilder.fromHttpUrl(this.elasticConfigData.getConnectionUrl()).build();
		return new RestHighLevelClient(RestClient
			.builder(new HttpHost(Objects.requireNonNull(serverUri.getHost()), serverUri.getPort(),
					serverUri.getScheme()))
			.setRequestConfigCallback(builder -> builder.setConnectTimeout(this.elasticConfigData.getConnectTimeoutMs())
				.setSocketTimeout(this.elasticConfigData.getSocketTimeoutMs())));
	}

	@Bean
	public ElasticsearchOperations elasticsearchOperations() {
		return new ElasticsearchRestTemplate(elasticsearchClient());
	}

}
