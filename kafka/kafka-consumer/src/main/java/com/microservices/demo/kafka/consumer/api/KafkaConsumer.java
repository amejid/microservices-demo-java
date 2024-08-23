package com.microservices.demo.kafka.consumer.api;

import java.util.List;

import org.apache.avro.specific.SpecificRecordBase;

public interface KafkaConsumer<T extends SpecificRecordBase> {

	void receive(List<T> messages, List<Long> keys, List<Integer> partitions, List<Long> offsets);

}
