package com.microservices.demo.elastic.query.web.client.config;

import java.util.concurrent.TimeUnit;

import com.microservices.demo.config.ElasticQueryWebClientConfigData;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@LoadBalancerClient(name = "elastic-query-service", configuration = ElasticQueryServiceInstanceListSupplierConfig.class)
public class WebClientConfig {

	@Value("${security.default-client-registration-id}")
	private String defaultClientRegistrationId;

	private final ElasticQueryWebClientConfigData.WebClient elasticQueryWebClientConfigData;

	public WebClientConfig(ElasticQueryWebClientConfigData elasticQueryWebClientConfigData) {
		this.elasticQueryWebClientConfigData = elasticQueryWebClientConfigData.getWebclient();
	}

	@LoadBalanced
	@Bean("webClientBuilder")
	WebClient.Builder webClientBuilder(ClientRegistrationRepository clientRegistrationRepository,
			OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository) {
		ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 = new ServletOAuth2AuthorizedClientExchangeFilterFunction(
				clientRegistrationRepository, oAuth2AuthorizedClientRepository);
		oauth2.setDefaultOAuth2AuthorizedClient(true);
		oauth2.setDefaultClientRegistrationId(this.defaultClientRegistrationId);
		return WebClient.builder()
			.baseUrl(this.elasticQueryWebClientConfigData.getBaseUrl())
			.defaultHeader(HttpHeaders.CONTENT_TYPE, this.elasticQueryWebClientConfigData.getContentType())
			.defaultHeader(HttpHeaders.ACCEPT, this.elasticQueryWebClientConfigData.getAcceptType())
			.clientConnector(new ReactorClientHttpConnector(HttpClient.from(getTcpClient())))
			.apply(oauth2.oauth2Configuration())
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
