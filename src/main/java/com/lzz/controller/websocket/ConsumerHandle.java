package com.lzz.controller.websocket;

/**
 * Created by lzz on 2018/1/16.
 */

import com.lzz.kafka.test.Consumer;
import com.lzz.logic.ToolService;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
public class ConsumerHandle implements WebSocketHandler {
    private Map<String, ToolService> toolServiceMap = new HashMap<>();
    private Logger log = LoggerFactory.getLogger(ConsumerHandle.class);
    private Consumer consumer;
    private static final ArrayList<WebSocketSession> users = new ArrayList<WebSocketSession>();
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("ConnectionEstablished");
        users.add(session);
        toolServiceMap.put(session.getId(), new ToolService());
        session.sendMessage(new TextMessage("connect"));

    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        JSONObject reqObject = JSONObject.fromObject(message.getPayload().toString());

        String topic = reqObject.getString("topic");
        String brokers = reqObject.getString("brokers");
        int partition = reqObject.getInt("partition");
        int consumerPartition = reqObject.getInt("consumer_partition");
        int offset = reqObject.getInt("offset");
        ToolService toolService = toolServiceMap.get(session.getId());
        toolService.startConsumer(session, brokers, topic, consumerPartition, partition, offset);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        if (session.isOpen()) {
            session.close();
        }
        stopConsumer(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        System.out.println("afterConnectionClosed " + closeStatus.getReason());
        stopConsumer(session);

    }

    public void stopConsumer(WebSocketSession session) {
        ToolService toolService = toolServiceMap.get(session.getId());
        if (null != toolService) {
            toolService.stopConsumer();
            toolServiceMap.remove(session.getId());
        }
        users.remove(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }


}