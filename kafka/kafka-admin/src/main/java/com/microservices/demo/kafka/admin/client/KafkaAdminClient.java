package com.microservices.demo.kafka.admin.client;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.microservices.demo.config.KafkaConfigData;
import com.microservices.demo.config.RetryConfigData;
import com.microservices.demo.kafka.admin.exception.KafkaClientException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class KafkaAdminClient {

	private static final Logger LOG = LoggerFactory.getLogger(KafkaAdminClient.class);

	private final KafkaConfigData kafkaConfigData;

	private final RetryConfigData retryConfigData;

	private final AdminClient adminClient;

	private final RetryTemplate retryTemplate;

	private final WebClient webClient;

	public KafkaAdminClient(KafkaConfigData kafkaConfigData, RetryConfigData retryConfigData, AdminClient adminClient,
			RetryTemplate retryTemplate, WebClient webClient) {
		this.kafkaConfigData = kafkaConfigData;
		this.retryConfigData = retryConfigData;
		this.adminClient = adminClient;
		this.retryTemplate = retryTemplate;
		this.webClient = webClient;
	}

	public void createTopics() {
		CreateTopicsResult createTopicsResult;
		try {
			createTopicsResult = this.retryTemplate.execute(this::doCreateTopics);
			LOG.info("Topics created: {}", createTopicsResult.values().values());
		}
		catch (Exception ex) {
			throw new KafkaClientException("Reached maximum number of retries while creating topics", ex);
		}
		checkTopicsCreated();
	}

	public void checkTopicsCreated() {
		Collection<TopicListing> topics = getTopics();
		int retryCount = 1;
		Integer maxRetries = this.retryConfigData.getMaxAttempts();
		int multiplier = this.retryConfigData.getMultiplier().intValue();
		Long sleepTimeMs = this.retryConfigData.getSleepTimeMs();

		for (String topic : this.kafkaConfigData.getTopicNamesToCreate()) {
			while (!isTopicCreated(topics, topic)) {
				checkMaxRetry(retryCount++, maxRetries);
				sleep(sleepTimeMs);
				sleepTimeMs *= multiplier;
				topics = getTopics();
			}
		}
	}

	public void checkSchemaRegistry() {
		int retryCount = 1;
		Integer maxRetries = this.retryConfigData.getMaxAttempts();
		int multiplier = this.retryConfigData.getMultiplier().intValue();
		Long sleepTimeMs = this.retryConfigData.getSleepTimeMs();

		while (!getSchemaRegistryStatus().is2xxSuccessful()) {
			checkMaxRetry(retryCount++, maxRetries);
			sleep(sleepTimeMs);
			sleepTimeMs *= multiplier;

		}
	}

	private HttpStatusCode getSchemaRegistryStatus() {
		try {
			return this.webClient.method(HttpMethod.GET)
				.uri(this.kafkaConfigData.getSchemaRegistryUrl())
				.exchangeToMono(response -> {
					if (response.statusCode().is2xxSuccessful()) {
						return Mono.just(response.statusCode());
					}
					else {
						return Mono.just(HttpStatus.SERVICE_UNAVAILABLE);
					}
				})
				.block();
		}
		catch (Exception ex) {
			return HttpStatus.SERVICE_UNAVAILABLE;
		}
	}

	private void sleep(Long sleepTimeMs) {
		try {
			Thread.sleep(sleepTimeMs);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new KafkaClientException("Error while sleeping for waiting new created topics", ex);
		}
	}

	private void checkMaxRetry(int retry, Integer maxRetries) {
		if (retry > maxRetries) {
			throw new KafkaClientException("Reached maximum number of retries while checking topics");
		}
	}

	private boolean isTopicCreated(Collection<TopicListing> topics, String topicName) {
		if (topics == null) {
			return false;
		}
		return topics.stream().anyMatch(topic -> topic.name().equals(topicName));
	}

	private CreateTopicsResult doCreateTopics(RetryContext retryContext) {
		List<String> topicNames = this.kafkaConfigData.getTopicNamesToCreate();
		LOG.info("Creating {} topic(s), attempt {}", topicNames.size(), retryContext.getRetryCount());
		List<NewTopic> newTopicList = topicNames.stream()
			.map(topicName -> new NewTopic(topicName.trim(), this.kafkaConfigData.getNumOfPartitions(),
					this.kafkaConfigData.getReplicationFactor()))
			.toList();

		return this.adminClient.createTopics(newTopicList);
	}

	private Collection<TopicListing> getTopics() {
		Collection<TopicListing> topics;

		try {
			topics = this.retryTemplate.execute(this::doGetTopics);
		}
		catch (Exception ex) {
			throw new KafkaClientException("Reached maximum number of retries while reading topics", ex);
		}

		return topics;
	}

	private Collection<TopicListing> doGetTopics(RetryContext retryContext)
			throws ExecutionException, InterruptedException {
		LOG.info("Reading topic {}, attempt {}", this.kafkaConfigData.getTopicNamesToCreate().toArray(),
				retryContext.getRetryCount());
		Collection<TopicListing> topics = this.adminClient.listTopics().listings().get();
		if (topics != null) {
			topics.forEach(topic -> LOG.debug("Topic with name: {}", topic.name()));
		}
		return topics;
	}

}
