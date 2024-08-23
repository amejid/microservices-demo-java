package com.microservices.demo.elastic.query.service.config;

import java.util.concurrent.TimeUnit;

import com.microservices.demo.config.ElasticQueryServiceConfigData;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

	private final ElasticQueryServiceConfigData.WebClient elasticQueryServiceConfigData;

	public WebClientConfig(ElasticQueryServiceConfigData elasticQueryServiceConfigData) {
		this.elasticQueryServiceConfigData = elasticQueryServiceConfigData.getWebClient();
	}

	@LoadBalanced
	@Bean("webClientBuilder")
	WebClient.Builder webClientBuilder() {
		return WebClient.builder()
			.defaultHeader(HttpHeaders.CONTENT_TYPE, this.elasticQueryServiceConfigData.getContentType())
			.defaultHeader(HttpHeaders.ACCEPT, this.elasticQueryServiceConfigData.getAcceptType())
			.clientConnector(new ReactorClientHttpConnector(getHttpClient()))
			.codecs(configurer -> configurer.defaultCodecs()
				.maxInMemorySize(this.elasticQueryServiceConfigData.getMaxInMemorySize()));
	}

	private HttpClient getHttpClient() {
		return HttpClient.create()
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, this.elasticQueryServiceConfigData.getConnectTimeoutMs())
			.doOnConnected(connection -> {
				connection.addHandlerLast(new ReadTimeoutHandler(this.elasticQueryServiceConfigData.getReadTimeoutMs(),
						TimeUnit.MILLISECONDS));
				connection.addHandlerLast(new WriteTimeoutHandler(
						this.elasticQueryServiceConfigData.getWriteTimeoutMs(), TimeUnit.MILLISECONDS));
			});
	}

}
