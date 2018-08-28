package com.lzz.kafka;

/**
 * Created by on 2018/1/19.
 */
import com.lzz.model.ThreadSwitch;
import com.lzz.util.TimeUtil;
import kafka.api.FetchRequest;
import kafka.api.FetchRequestBuilder;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.common.ErrorMapping;
import kafka.common.TopicAndPartition;
import kafka.javaapi.*;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.message.MessageAndOffset;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import java.nio.ByteBuffer;
import java.util.*;


/**
 * Created by gl49 on 2017/12/14.
 */
@Component
public class KafkaConsumerUtil {
    private static Logger logger = Logger.getLogger(KafkaConsumerUtil.class);

    private List<String> m_replicaBrokers = new ArrayList();
    private String topic = "redis-service-test";
    private int partition = 0;
    private long offset = -1;
    private int port = 9093;
    private String clientName;
    private List<String> seeds = new ArrayList();
    private ThreadSwitch threadSwitch;

    public KafkaConsumerUtil(){

    }

    public KafkaConsumerUtil(ThreadSwitch threadSwitch, List<String> seeds, int port, String topic, String groupid, int partition, long offset){
        this.seeds = seeds;
        this.port = port;
        this.topic = topic;
        this.partition = partition;
        this.offset = offset;
        this.clientName = groupid;
        this.threadSwitch = threadSwitch;
    }


    public void execute( ConsumerRunnable task ){
        runConsumer(this.topic, this.partition, this.seeds, this.port, this.offset, task);
    }


    public void runConsumer(String a_topic, int a_partition, List<String> a_seedBrokers, int a_port, long readOffset, ConsumerRunnable task){

        // find the meta data about the topic and partition we are interested in
        PartitionMetadata metadata = findLeader(a_seedBrokers, a_port, a_topic, a_partition);
        if (metadata == null) {
            logger.error("Can't find metadata for Topic and Partition. Exiting");
            return;
        }
        if (metadata.leader() == null) {
            logger.error("Can't find Leader for Topic and Partition. Exiting");
            return;
        }
        String leadBroker = metadata.leader().host();
        String clientName = "Client_" + a_topic + "_" + a_partition;

        SimpleConsumer consumer = new SimpleConsumer(leadBroker, a_port, 100000, 64 * 1024, clientName);
        if( 0 == readOffset){
            readOffset = getLastOffset(consumer,a_topic, a_partition, kafka.api.OffsetRequest.EarliestTime(), clientName);
        }

        int numErrors = 0;
        long startTime = System.currentTimeMillis();
        long readTotalCount = 0;
        while ( this.threadSwitch.isStart() ) {
            try {
                if (consumer == null) {
                    consumer = new SimpleConsumer(leadBroker, a_port, 100000, 1024 * 1024, clientName);
                }
                FetchRequest req = new FetchRequestBuilder()
                        .clientId(clientName)
                        .addFetch(a_topic, a_partition, readOffset, 1024 * 1024) // Note: this fetchSize of 100000 might need to be increased if large batches are written to Kafka
                        .build();
                FetchResponse fetchResponse = consumer.fetch(req);

                if (fetchResponse.hasError()) {
                    numErrors++;
                    // Something went wrong!
                    short code = fetchResponse.errorCode(a_topic, a_partition);
                    logger.error("Error fetching data from the Broker:" + leadBroker + " Reason: " + code);
                    if (numErrors > 5) break;
                    if (code == ErrorMapping.OffsetOutOfRangeCode())  {
                        // We asked for an invalid offset. For simple case ask for the last element to reset
                        readOffset = getLastOffset(consumer,a_topic, a_partition, kafka.api.OffsetRequest.LatestTime(), clientName);
                        continue;
                    }
                    consumer.close();
                    consumer = null;
                    leadBroker = findNewLeader(leadBroker, a_topic, a_partition, a_port);
                    continue;
                }
                numErrors = 0;

                long numRead = 0;
                for (MessageAndOffset messageAndOffset : fetchResponse.messageSet(a_topic, a_partition)) {
                    try {
                        long currentOffset = messageAndOffset.offset();
                        if (currentOffset < readOffset) {
                            logger.info("Found an old offset: " + currentOffset + " Expecting: " + readOffset);
                            continue;
                        }
                        readOffset = messageAndOffset.nextOffset();
                        ByteBuffer payload = messageAndOffset.message().payload();

                        byte[] bytes = new byte[payload.limit()];
                        payload.get(bytes);
                        String msg = new String(bytes, "UTF-8");
                        if( msg.length() > 500 ){
                            msg = msg.substring(0, 500) + ".......";
                        }
                        // 一秒十条发送
                        if( readTotalCount < 200 || (System.currentTimeMillis() - startTime)/100 > readTotalCount ){
                            task.consumer( messageAndOffset.offset(), msg );
                            readTotalCount++;
                        }
                        numRead++;
                    }catch (Exception ignore){

                    }
                }

                if (numRead == 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        logger.error("thread sleep exception", ie);
                    }
                }
            }catch (Exception e){
                logger.error("while true exeception ", e);
            }finally {
                if( System.currentTimeMillis() - startTime >=  10 * 60 * 1000){
                    break;
                }
            }
        }
        if (consumer != null) consumer.close();
    }


    public static long getLastOffset(SimpleConsumer consumer, String topic, int partition,
                                     long whichTime, String clientName) {
        TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);
        Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
        requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(whichTime, 1));
        kafka.javaapi.OffsetRequest request = new kafka.javaapi.OffsetRequest(
                requestInfo, kafka.api.OffsetRequest.CurrentVersion(), clientName);
        OffsetResponse response = consumer.getOffsetsBefore(request);

        if (response.hasError()) {
            System.out.println("Error fetching data Offset Data the Broker. Reason: " + response.errorCode(topic, partition) );
            return 0;
        }
        long[] offsets = response.offsets(topic, partition);
        return offsets[0];
    }

    private String findNewLeader(String a_oldLeader, String a_topic, int a_partition, int a_port) throws Exception {
        for (int i = 0; i < 3; i++) {
            boolean goToSleep = false;
            PartitionMetadata metadata = findLeader(m_replicaBrokers, a_port, a_topic, a_partition);
            if (metadata == null) {
                goToSleep = true;
            } else if (metadata.leader() == null) {
                goToSleep = true;
            } else if (a_oldLeader.equalsIgnoreCase(metadata.leader().host()) && i == 0) {
                // first time through if the leader hasn't changed give ZooKeeper a second to recover
                // second time, assume the broker did recover before failover, or it was a non-Broker issue
                goToSleep = true;
            } else {
                return metadata.leader().host();
            }
            if (goToSleep) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                }
            }
        }
        throw new Exception("Unable to find new leader after Broker failure. Exiting");
    }

    private PartitionMetadata findLeader(List<String> a_seedBrokers, int a_port, String a_topic, int a_partition) {
        PartitionMetadata returnMetaData = null;
        loop:
        for (String seed : a_seedBrokers) {
            SimpleConsumer consumer = null;
            try {
                consumer = new SimpleConsumer(seed, a_port, 100000, 64 * 1024, "leaderLookup");
                List<String> topics = Collections.singletonList(a_topic);
                TopicMetadataRequest req = new TopicMetadataRequest(topics);
                kafka.javaapi.TopicMetadataResponse resp = consumer.send(req);

                List<TopicMetadata> metaData = resp.topicsMetadata();
                for (TopicMetadata item : metaData) {
                    for (PartitionMetadata part : item.partitionsMetadata()) {
                        if (part.partitionId() == a_partition) {
                            returnMetaData = part;
                            break loop;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error communicating with Broker [" + seed + "] to find Leader for [" + a_topic
                        + ", " + a_partition + "] Reason: " + e);
            } finally {
                if (consumer != null) consumer.close();
            }
        }
        if (returnMetaData != null) {
            m_replicaBrokers.clear();
            for (kafka.cluster.Broker replica : returnMetaData.replicas()) {
                m_replicaBrokers.add(replica.host());
            }
        }
        return returnMetaData;
    }

    public String getTopic() {
        return topic;
    }

    public int getPartition() {
        return partition;
    }

    public String getClientName() {
        return clientName;
    }

    public static String getClientName(String topic, int partition) {
        return "Client_" + topic + "_" + partition;
    }


    class PartitionConsumer implements Runnable{
        private KafkaConsumerUtil kafkaConsumerUtil;
        private ConsumerRunnable consumerRunnable;
        private int partition;

        public PartitionConsumer(KafkaConsumerUtil kafkaConsumerUtil, ConsumerRunnable consumerRunnable, int partition) {
            this.kafkaConsumerUtil = kafkaConsumerUtil;
            this.consumerRunnable = consumerRunnable;
            this.partition = partition;
        }

        @Override
        public void run() {
            try {
                System.out.println( "------------" + kafkaConsumerUtil.offset + "-------" + this.partition);
                runConsumer(kafkaConsumerUtil.topic, this.partition, kafkaConsumerUtil.seeds, kafkaConsumerUtil.port, kafkaConsumerUtil.offset, this.consumerRunnable);
            }catch (Exception e){
                logger.error( "partition consumer : ", e );
            }
        }
    }
}
