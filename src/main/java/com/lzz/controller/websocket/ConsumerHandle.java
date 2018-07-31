package com.lzz.controller.websocket;

/**
 * Created by lzz on 2018/1/16.
 */

import com.lzz.kafka.Consumer;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.ArrayList;
import java.util.Date;

@Component
@Scope("singleton")
public class ConsumerHandle implements WebSocketHandler {

    private Logger log = LoggerFactory.getLogger(ConsumerHandle.class);

    private static final ArrayList<WebSocketSession> users = new ArrayList<WebSocketSession>();;


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("ConnectionEstablished");
        log.debug("ConnectionEstablished");
        users.add(session);

        session.sendMessage(new TextMessage("connect"));
        session.sendMessage(new TextMessage("new_msg"));

    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        JSONObject reqObject = JSONObject.fromObject( message.getPayload().toString() );
        session.sendMessage(new TextMessage(new Date() + "" + reqObject.toString()));
        String topic = reqObject.getString("topic");
        int runTime = reqObject.getInt("runtime");
        Consumer consumer = new Consumer(topic);
        consumer.consumer(session, runTime);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        if(session.isOpen()){
            session.close();
        }
        users.remove(session);

        log.debug("handleTransportError" + exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        users.remove(session);
        System.out.println("afterConnectionClosed" + closeStatus.getReason());

    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }


}
