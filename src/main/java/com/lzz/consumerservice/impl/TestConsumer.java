package com.lzz.consumerservice.impl;

import com.lzz.consumerservice.ConsumerService;
import com.lzz.consumerservice.ConsumerTask;

/**
 * Created by gl49 on 2018/3/16.
 */
public class TestConsumer extends ConsumerService {

    @Override
    public void consumer() {
        this.streamProcess(new ConsumerTask() {
            @Override
            public void msgProcess(String message) {
                System.out.println( "consumer : " +  message );
            }
        });
    }
}
