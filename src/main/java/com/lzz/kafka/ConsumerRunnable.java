package com.lzz.kafka;

/**
 * Created by gl49 on 2017/12/15.
 */
public interface ConsumerRunnable {
    public void consumer(long offset, String msg);
}
