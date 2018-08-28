package com.lzz.kafka.test;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by lzz on 2018/1/18.
 */
public class Consumer {
    private String topic;
    public Consumer(String topic){
        this.topic = topic;
    }

    public void consumer(WebSocketSession session, int runTime) throws IOException, InterruptedException {
        if( runTime > 30 ){
            runTime = 30;
        }
        runTime = runTime * 60 * 1000;
        long startTime = System.currentTimeMillis();
        while (true){
            Thread.sleep(500);
            LinkedBlockingDeque<String> mq = Topic.getMq(this.topic);
            String msg = mq.take();
            System.out.println("consumer msg...." + msg);
            session.sendMessage(new TextMessage( msg ));
            if( System.currentTimeMillis() - startTime >=  runTime){
                break;
            }
        }
    }
}
