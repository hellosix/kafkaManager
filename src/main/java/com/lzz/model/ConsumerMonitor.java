package com.lzz.model;

/**
 * Created by gl49 on 2018/3/16.
 */
public class ConsumerMonitor {
    private String zookeeper;
    private String groupId;
    private String topic;
    private long  msgNum;
    private int consumerNum;

    public String getZookeeper() {
        return zookeeper;
    }

    public void setZookeeper(String zookeeper) {
        this.zookeeper = zookeeper;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getMsgNum() {
        return msgNum;
    }

    public void setMsgNum(long msgNum) {
        this.msgNum = msgNum;
    }

    public int getConsumerNum() {
        return consumerNum;
    }

    public void setConsumerNum(int consumerNum) {
        this.consumerNum = consumerNum;
    }
}
