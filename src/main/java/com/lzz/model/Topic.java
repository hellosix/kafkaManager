package com.lzz.model;

/**
 * Created by gl49 on 2018/1/19.
 */
public class Topic {
    private String topic;
    private int partition;
    private int replication;

    public Topic(String topic, int partition, int replication) {
        this.topic = topic;
        this.partition = partition;
        this.replication = replication;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public int getReplication() {
        return replication;
    }

    public void setReplication(int replication) {
        this.replication = replication;
    }

    @Override
    public String toString() {
        return "Topic{" +
                "topic='" + topic + '\'' +
                ", partition=" + partition +
                ", replication=" + replication +
                '}';
    }
}
