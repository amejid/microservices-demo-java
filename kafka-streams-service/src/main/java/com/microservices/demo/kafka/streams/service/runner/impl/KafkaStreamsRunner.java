package com.microservices.demo.kafka.streams.service.runner.impl;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import com.microservices.demo.config.KafkaConfigData;
import com.microservices.demo.config.KafkaStreamsConfigData;
import com.microservices.demo.kafka.avro.model.TwitterAnalyticsAvroModel;
import com.microservices.demo.kafka.avro.model.TwitterAvroModel;
import com.microservices.demo.kafka.streams.service.runner.StreamsRunner;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class KafkaStreamsRunner implements StreamsRunner<String, Long> {

	private static final Logger LOG = LoggerFactory.getLogger(KafkaStreamsRunner.class);

	private static final String REGEX = "\\W+";

	private final KafkaStreamsConfigData kafkaStreamsConfigData;

	private final KafkaConfigData kafkaConfigData;

	private final Properties streamsConfiguration;

	private KafkaStreams kafkaStreams;

	private volatile ReadOnlyKeyValueStore<String, Long> keyValueStore;

	public KafkaStreamsRunner(KafkaStreamsConfigData kafkaStreamsConfigData, KafkaConfigData kafkaConfigData,
			@Qualifier("streamConfiguration") Properties streamsConfiguration) {
		this.kafkaStreamsConfigData = kafkaStreamsConfigData;
		this.kafkaConfigData = kafkaConfigData;
		this.streamsConfiguration = streamsConfiguration;
	}

	@Override
	public void start() {
		final Map<String, String> serdeConfig = Collections.singletonMap(this.kafkaConfigData.getSchemaRegistryUrlKey(),
				this.kafkaConfigData.getSchemaRegistryUrl());
		final StreamsBuilder streamsBuilder = new StreamsBuilder();
		KStream<Long, TwitterAvroModel> twitterAvroModelStream = getTwitterAvroModelStream(serdeConfig, streamsBuilder);

		createTopology(twitterAvroModelStream, serdeConfig);

		startStreaming(streamsBuilder);
	}

	@Override
	public Long getValueByKey(String word) {
		if (this.kafkaStreams != null && this.kafkaStreams.state() == KafkaStreams.State.RUNNING) {
			if (this.keyValueStore == null) {
				synchronized (this) {
					if (this.keyValueStore == null) {
						this.keyValueStore = this.kafkaStreams.store(StoreQueryParameters.fromNameAndType(
								this.kafkaStreamsConfigData.getWordCountStoreName(),
								QueryableStoreTypes.keyValueStore()));
					}
				}
			}
			return this.keyValueStore.get(word.toLowerCase());
		}
		return 0L;
	}

	@PreDestroy
	public void close() {
		if (this.kafkaStreams != null) {
			this.kafkaStreams.close();
			LOG.info("Kafka Streams stopped");
		}
	}

	private void startStreaming(StreamsBuilder streamsBuilder) {
		final Topology topology = streamsBuilder.build();
		LOG.info("Topology description: {}", topology.describe());
		this.kafkaStreams = new KafkaStreams(topology, this.streamsConfiguration);
		this.kafkaStreams.start();
		LOG.info("Kafka Streams started");
	}

	private void createTopology(KStream<Long, TwitterAvroModel> twitterAvroModelStream,
			Map<String, String> serdeConfig) {
		Pattern pattern = Pattern.compile(REGEX, Pattern.UNICODE_CHARACTER_CLASS);

		Serde<TwitterAnalyticsAvroModel> serdeTwitterAnalyticsAvroModel = getSerdeAnalyticsModel(serdeConfig);

		twitterAvroModelStream.flatMapValues(value -> Arrays.asList(pattern.split(value.getText().toLowerCase())))
			.groupBy((key, word) -> word)
			.count(Materialized.as(this.kafkaStreamsConfigData.getWordCountStoreName()))
			.toStream()
			.map(mapToAnalyticsModel())
			.to(this.kafkaStreamsConfigData.getOutputTopicName(),
					Produced.with(Serdes.String(), serdeTwitterAnalyticsAvroModel));
	}

	private KeyValueMapper<String, Long, KeyValue<String, ? extends TwitterAnalyticsAvroModel>> mapToAnalyticsModel() {
		return (word, count) -> {
			LOG.info("Sending to topic {} word {} count {}", this.kafkaStreamsConfigData.getOutputTopicName(), word,
					count);
			return new KeyValue<>(word,
					TwitterAnalyticsAvroModel.newBuilder()
						.setWord(word)
						.setWordCount(count)
						.setCreatedAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
						.build());
		};
	}

	private KStream<Long, TwitterAvroModel> getTwitterAvroModelStream(Map<String, String> serdeConfig,
			StreamsBuilder streamsBuilder) {
		final Serde<TwitterAvroModel> serdeTwitterAvroModel = new SpecificAvroSerde<>();
		serdeTwitterAvroModel.configure(serdeConfig, false);
		return streamsBuilder.stream(this.kafkaStreamsConfigData.getInputTopicName(),
				Consumed.with(Serdes.Long(), serdeTwitterAvroModel));
	}

	private Serde<TwitterAnalyticsAvroModel> getSerdeAnalyticsModel(Map<String, String> serdeConfig) {
		Serde<TwitterAnalyticsAvroModel> serdeTwitterAnalyticsAvroModel = new SpecificAvroSerde<>();
		serdeTwitterAnalyticsAvroModel.configure(serdeConfig, false);
		return serdeTwitterAnalyticsAvroModel;
	}

}
