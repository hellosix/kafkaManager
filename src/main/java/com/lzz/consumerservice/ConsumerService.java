package com.lzz.consumerservice;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by gl49 on 2018/3/16.
 */
public abstract class ConsumerService{
    protected AtomicLong msgNum = new AtomicLong(0);

    protected List<KafkaStream<byte[], byte[]>> streams;

    public abstract void consumer();
    public void streamProcess(ConsumerTask task) {
        List<KafkaStream<byte[], byte[]>> streams = getStreams();
        for (final KafkaStream stream : streams) {
            ConsumerSchedule.executorService.submit(new Runnable() {
                @Override
                public void run() {
                    ConsumerIterator<byte[], byte[]> it = stream.iterator();
                    while (true){
                        boolean hasMsg = it.hasNext();
                        if( !hasMsg ){
                            try {
                                Thread.sleep( 20 );
                            } catch (InterruptedException ignore) {

                            }
                        }
                        msgNumIncrement();
                        task.msgProcess( new String(it.next().message()) );
                    }
                }
            });
        }
    }

    public List<KafkaStream<byte[], byte[]>> getStreams() {
        return streams;
    }

    public void setStreams(List<KafkaStream<byte[], byte[]>> streams) {
        this.streams = streams;
    }

    public AtomicLong getMsgNum() {
        return msgNum;
    }

    public void msgNumIncrement(){
        this.msgNum.getAndIncrement();
    }
}
