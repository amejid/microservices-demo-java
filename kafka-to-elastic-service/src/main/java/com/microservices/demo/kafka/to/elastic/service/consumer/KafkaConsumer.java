package com.microservices.demo.kafka.to.elastic.service.consumer;

import java.io.Serializable;
import java.util.List;

import org.apache.avro.specific.SpecificRecordBase;

public interface KafkaConsumer<K extends Serializable, V extends SpecificRecordBase> {

	void receive(List<V> messages, List<Integer> keys, List<Integer> partitions, List<K> offsets);

}
