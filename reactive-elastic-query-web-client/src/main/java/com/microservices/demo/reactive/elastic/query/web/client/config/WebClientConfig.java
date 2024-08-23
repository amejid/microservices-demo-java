package com.microservices.demo.reactive.elastic.query.web.client.config;

import java.util.concurrent.TimeUnit;

import com.microservices.demo.config.ElasticQueryWebClientConfigData;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

	private final ElasticQueryWebClientConfigData.WebClient webClientConfig;

	public WebClientConfig(ElasticQueryWebClientConfigData webClientConfigData) {
		this.webClientConfig = webClientConfigData.getWebclient();
	}

	@Bean("webClient")
	public WebClient webClient() {
		return WebClient.builder()
			.baseUrl(this.webClientConfig.getBaseUrl())
			.defaultHeader(HttpHeaders.CONTENT_TYPE, this.webClientConfig.getContentType())
			.clientConnector(new ReactorClientHttpConnector(getHttpClient()))
			.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(this.webClientConfig.getMaxInMemorySize()))
			.build();
	}

	private HttpClient getHttpClient() {
		return HttpClient.create()
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, this.webClientConfig.getConnectTimeoutMs())
			.doOnConnected(connection -> {
				connection.addHandlerLast(
						new ReadTimeoutHandler(this.webClientConfig.getReadTimeoutMs(), TimeUnit.MILLISECONDS));
				connection.addHandlerLast(
						new WriteTimeoutHandler(this.webClientConfig.getWriteTimeoutMs(), TimeUnit.MILLISECONDS));
			});
	}

}
