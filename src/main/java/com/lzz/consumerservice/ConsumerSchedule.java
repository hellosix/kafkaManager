package com.lzz.consumerservice;

import com.lzz.consumerservice.impl.TestConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by gl49 on 2018/3/16.
 */
public class ConsumerSchedule {
    public static ExecutorService executorService = Executors.newFixedThreadPool(100);
    public static List<ConsumerTemplet> consumerServiceList = new ArrayList<>();
    static {
        consumerServiceList.add( new ConsumerTemplet("*:2181", "hhqh2", "test00001", new TestConsumer(), 3) );
        consumerServiceList.add( new ConsumerTemplet("*01:2181", "hhqh", "test00001", new TestConsumer(), 4) );
    }

    public static boolean startAllConsumer(){
        for(int i = 0; i < consumerServiceList.size(); i++){
            ConsumerTemplet consumerTemplet = consumerServiceList.get( i );
            consumerTemplet.run();
        }
        return true;
    }
}
