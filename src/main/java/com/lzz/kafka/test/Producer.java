package com.lzz.kafka.test;

/**
 * Created by lzz on 2018/1/18.
 */
public class Producer {
    private String topic;
    public Producer(String topic){
        this.topic = topic;
    }

    public boolean send(String msg){
        return Topic.addMsg( this.topic, msg );
    }
}
