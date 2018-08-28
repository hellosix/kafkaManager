package com.lzz.kafka;


import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.util.Properties;

/**
 * Created by gl49 on 2017/12/14.
 */
@Component
public class KafkaProducer implements Closeable {

    static final String SERIALIZER_CLASS_NAME = "serializer.class";
    static final String SERIALIZER_CLASS_VALUE = "kafka.serializer.StringEncoder";
    static final String REQUEST_REQUIRED_ACKS_NAME = "request.required.acks";
    static final String REQUEST_REQUIRED_ACKS_VALUE = "-1";
    static final String PARTITION_CLASS_NAME = "partitioner.class";
    static final String PARTITION_CLASS_VALUE = "kafka.producer.DefaultPartitioner";
    static final String METADATA_BROKER_LIST_NAME = "metadata.broker.list";
    static final String PRODUCER_TYPE = "producer.type";
    static final String PRODUCER_TYPE_VALUE = "sync";

    private Producer<String, String> producer;
    private String topic;
    private Properties properties = new Properties();

    public KafkaProducer(){

    }

    public KafkaProducer(String brokerList, String topic) {

        properties.put(SERIALIZER_CLASS_NAME, SERIALIZER_CLASS_VALUE);
        properties.put(REQUEST_REQUIRED_ACKS_NAME, REQUEST_REQUIRED_ACKS_VALUE);
        properties.put(METADATA_BROKER_LIST_NAME, brokerList);
        properties.put(PARTITION_CLASS_NAME, PARTITION_CLASS_VALUE);
        properties.put(PRODUCER_TYPE, PRODUCER_TYPE_VALUE);
        this.topic = topic;
        this.producer = new Producer<>(new ProducerConfig(properties));
    }

    public KafkaProducer(Properties properties, String topic) {
        this.properties = properties;
        this.topic = topic;
        this.producer = new Producer<>(new ProducerConfig(this.properties));
    }

    /**
     *
     */
    public void append(String message) {
        KeyedMessage<String, String> msg = new KeyedMessage<>(topic, message);
        append(msg);
    }

    /**
     */
    public void appendByKey(String message) {
        KeyedMessage<String, String> msg = new KeyedMessage<>(topic, message, message);
        append(msg);
    }

    /**
     *
     * @param msg
     */
    private void append(KeyedMessage<String, String> msg) {

        if (producer == null)
            throw new RuntimeException("producer is closed");
        producer.send(msg);
    }

    /**
     *
     */
    public void close() {
        if (producer != null) {
            producer.close();
            producer = null;
        }
    }
}
