package com.lzz.model;

/**
 * Created by gl49 on 2018/1/20.
 */
public class Consumer {
    private int partitions;
    private String consumer;
    private String topic;

    public Consumer(String consumer, String topic, int partitions){
        this.consumer = consumer;
        this.topic = topic;
        this.partitions =partitions;
    }

    public int getPartitions() {
        return partitions;
    }

    public void setPartitions(int partitions) {
        this.partitions = partitions;
    }

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
