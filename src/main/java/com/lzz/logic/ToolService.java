package com.lzz.logic;

import com.lzz.KafkaTool;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lzz on 2018/1/14.
 */
@Component
public class ToolService {
    ExecutorService threadPool = Executors.newFixedThreadPool(200);

    public void startConsumer(JSONObject requestBody) {
        System.out.println( requestBody + "--------");
    }

    public void createTopic(JSONObject requestBody) {
        String zkAddress = requestBody.getString("zk_address");
        String topic = requestBody.getString("topic");
        int partition = requestBody.getInt("partition");
        int replication = requestBody.getInt("replication");
        KafkaTool.createTopic(zkAddress, topic, partition, replication);
    }
}
