package com.microservices.demo.elastic.query.web.client.config;

import java.util.concurrent.TimeUnit;

import com.microservices.demo.config.ElasticQueryWebClientConfigData;
import com.microservices.demo.config.UserConfigData;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@LoadBalancerClient(name = "elastic-query-service", configuration = ElasticQueryServiceInstanceListSupplierConfig.class)
public class WebClientConfig {

	private final ElasticQueryWebClientConfigData.WebClient elasticQueryWebClientConfigData;

	private final UserConfigData userConfigData;

	public WebClientConfig(ElasticQueryWebClientConfigData elasticQueryWebClientConfigData,
			UserConfigData userConfigData) {
		this.elasticQueryWebClientConfigData = elasticQueryWebClientConfigData.getWebclient();
		this.userConfigData = userConfigData;
	}

	@LoadBalanced
	@Bean("webClientBuilder")
	WebClient.Builder webClientBuilder() {
		return WebClient.builder()
			.filter(ExchangeFilterFunctions.basicAuthentication(this.userConfigData.getUsername(),
					this.userConfigData.getPassword()))
			.baseUrl(this.elasticQueryWebClientConfigData.getBaseUrl())
			.defaultHeader(HttpHeaders.CONTENT_TYPE, this.elasticQueryWebClientConfigData.getContentType())
			.defaultHeader(HttpHeaders.ACCEPT, this.elasticQueryWebClientConfigData.getAcceptType())
			.clientConnector(new ReactorClientHttpConnector(HttpClient.from(getTcpClient())))
			.codecs(configurer -> configurer.defaultCodecs()
				.maxInMemorySize(this.elasticQueryWebClientConfigData.getMaxInMemorySize()));
	}

	private TcpClient getTcpClient() {
		return TcpClient.create()
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, this.elasticQueryWebClientConfigData.getConnectTimeoutMs())
			.doOnConnected(connection -> {
				connection.addHandlerLast(new ReadTimeoutHandler(
						this.elasticQueryWebClientConfigData.getReadTimeoutMs(), TimeUnit.MILLISECONDS));
				connection.addHandlerLast(new WriteTimeoutHandler(
						this.elasticQueryWebClientConfigData.getWriteTimeoutMs(), TimeUnit.MILLISECONDS));
			});
	}

}
