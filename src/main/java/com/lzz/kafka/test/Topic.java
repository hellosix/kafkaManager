package com.lzz.kafka.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by lzz on 2018/1/18.
 */
public class Topic {
    private static Map<String, LinkedBlockingDeque<String>> mq = new HashMap<>();

    public static void createTopic(String topic){
        System.out.println("create topic");
        if( null == mq.get(topic) ){
            mq.put(topic, new LinkedBlockingDeque());
        }
    }

    public static List<String> getTopicList(){
        return new ArrayList<>();
    }

    public static boolean addMsg(String topic, String msg){
        System.out.println( "add msessage topic " + topic + " msg :" + msg );
        LinkedBlockingDeque deque = mq.get(topic);
        deque.add( msg );
        return true;
    }

    public static LinkedBlockingDeque getMq(String topic){
        System.out.println(" get queue " + mq.get(topic).size());
        return mq.get(topic);
    }
}
