package com.lzz.logic;

import com.lzz.consumerservice.ConsumerSchedule;
import com.lzz.consumerservice.ConsumerTemplet;
import com.lzz.dao.CuratorZookeeperClient;
import com.lzz.kafka.ConsumerRunnable;
import com.lzz.kafka.KafkaConsumerUtil;
import com.lzz.kafka.KafkaProducer;
import com.lzz.model.*;
import com.lzz.util.KafkaHelper;
import com.lzz.dao.ZookeeperClient;
import com.lzz.util.TimeUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.data.Stat;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by lzz on 2018/1/14.
 */
@Component
public class ToolService {
    public static final String TOPIC_PATH =  "/brokers/topics";
    public static final String BROKERS_PATH =  "/brokers/ids";
    public static final String CONSUMERS_PATH = "/consumers";

    private ExecutorService threadPool = Executors.newCachedThreadPool();
    private BlockingQueue<String> msgQueue = new LinkedBlockingDeque<>(5);
    public  ThreadSwitch threadSwitch = new ThreadSwitch();

    @Resource
    private KafkaProducer kafkaProducer;


    public List<Topic> getTopicList(String zk){
        List<Topic> topicList = new ArrayList<>();
        ZookeeperClient curatorZookeeperClient = null;
        try {
            curatorZookeeperClient = new CuratorZookeeperClient(zk, 10000);
            try {
                List<String> topics = curatorZookeeperClient.listChildrenNode( TOPIC_PATH );
                for(String topicPath : topics){
                    try {
                        byte[] resByte = curatorZookeeperClient.getData( TOPIC_PATH + "/" + topicPath );
                        String resStr = new String(resByte, StandardCharsets.UTF_8);
                        JSONObject jsonObject = JSONObject.fromObject(resStr);
                        JSONObject partitionList = jsonObject.getJSONObject("partitions");
                        JSONArray replication = partitionList.getJSONArray("0");
                        topicList.add( new Topic(topicPath, partitionList.size(), replication.size()));
                    }catch (Exception ignore){

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }finally {
            curatorZookeeperClient.shutDown();
        }
        return topicList;
    }

    public List<Broker> getBrokerList(String zk){
        ZookeeperClient curatorZookeeperClient = null;
        List<Broker> brokerList = new ArrayList<>();
        if( StringUtils.isBlank( zk ) ){
            return brokerList;
        }
        try {
            curatorZookeeperClient = new CuratorZookeeperClient(zk, 10000);
            try {
                List<String> brokers = curatorZookeeperClient.listChildrenNode( BROKERS_PATH );
                for(String brokerPath : brokers){
                    try {
                        byte[] resByte = curatorZookeeperClient.getData( BROKERS_PATH + "/" + brokerPath );
                        String resStr = new String(resByte, StandardCharsets.UTF_8);
                        JSONObject jsonObject = JSONObject.fromObject(resStr);
                        String host = jsonObject.getString("host");
                        String port = jsonObject.getString("port");
                        String jmx_port = jsonObject.getString("jmx_port");
                        brokerList.add( new Broker(host, port, jmx_port));
                    }catch (Exception ignore){

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }finally {
            curatorZookeeperClient.shutDown();
        }
        return brokerList;
    }

    public List<Consumer> getConsumerGroups(String zk) {
        ZookeeperClient curatorZookeeperClient = null;
        List<Consumer> consumerList = new ArrayList<>();
        if( StringUtils.isBlank( zk ) ){
            return consumerList;
        }
        try {
            curatorZookeeperClient = new CuratorZookeeperClient(zk, 10000);
            List<String> consumers = curatorZookeeperClient.listChildrenNode( CONSUMERS_PATH );
            for(String consumerPath : consumers){
                try {
                    String offsetPath = CONSUMERS_PATH + "/" + consumerPath + "/offsets";
                    List<String> topics = curatorZookeeperClient.listChildrenNode( offsetPath );
                    for(String topicPath : topics){
                        try {
                            String partitionPath = offsetPath + "/" + topicPath;
                            Stat stat = curatorZookeeperClient.getDataStat( partitionPath );
                            int partitions = stat.getNumChildren();
                            consumerList.add( new Consumer(consumerPath, topicPath, partitions));
                        }catch (Exception e){
                        }
                    }
                }catch (Exception e){
                }
            }
        }catch (Exception e){

        }finally {
            curatorZookeeperClient.shutDown();
        }
        return consumerList;
    }

    public List<Map<String,String>> getConsumerDetail(String zk,String broker, String topic, String consumer, int partitions) throws Exception {
        ZookeeperClient curatorZookeeperClient = null;
        List<Map<String,String>> resultList = new ArrayList<>();
        try {
            curatorZookeeperClient = new CuratorZookeeperClient(zk, 10000);
            for(int partition = 0; partition < partitions; partition++){
                try {
                    Map<String,String> result = new HashMap<>();
                    String partitionPath = "/consumers/" + consumer + "/offsets/" + topic + "/" + partition;
                    byte[] resByte = curatorZookeeperClient.getData( partitionPath );
                    Stat resStat = curatorZookeeperClient.getDataStat( partitionPath );
                    long mtime = resStat.getMtime();
                    String offsetStr = new String(resByte, StandardCharsets.UTF_8);
                    long logSize = KafkaHelper.getLogSize(broker, topic, partition);
                    result.put("partition", String.valueOf(partition));
                    result.put("date", TimeUtil.timeFormat(mtime));
                    result.put("topic", topic);
                    result.put("offset", offsetStr);
                    result.put("logSize", String.valueOf(logSize));
                    long lag = logSize - Long.parseLong(offsetStr);
                    result.put("lag", String.valueOf(lag));
                    resultList.add( result );
                }catch (Exception e){
                    throw e;
                }
            }
        }catch (Exception e1){
            throw e1;
        }finally {
            curatorZookeeperClient.shutDown();
        }
        return resultList;
    }

    public void createTopic(JSONObject requestBody) {
        String zkAddress = requestBody.getString("zk");
        String topic = requestBody.getString("topic");
        int partition = requestBody.getInt("partition");
        int replication = requestBody.getInt("replication");
        KafkaHelper.createTopic(zkAddress, topic, partition, replication);
    }

    public void deleteTopic(String zk, String topic) {
        KafkaHelper.deleteTopic( zk, topic );
    }

    public void startConsumer(WebSocketSession session, String brokers, String topic, int consumerPartition, int partition, int offset) throws IOException, InterruptedException {
        threadSwitch.setStart(true);
        Map<String, Object> seedsAndPort = getSeedsAndPort(brokers);
        String port = String.valueOf(seedsAndPort.get("port"));
        List<String> seeds = (List<String>) seedsAndPort.get("seeds");
        if( consumerPartition == -1 ){
            for(int i = 0; i < partition; i++){
                long logSize = KafkaHelper.getLogSize(seeds.get(0),Integer.parseInt(port), topic, partition);
                long tmpOffset = 0;
                if( logSize > 2 ){
                    tmpOffset = logSize - 2;
                }
                KafkaConsumerUtil kafkaConsumerUtil = new KafkaConsumerUtil(threadSwitch, seeds, Integer.parseInt(port), topic, "kafka-tool", i, tmpOffset);
                threadPool.submit( new ConsumerTask(msgQueue, kafkaConsumerUtil) );
            }
        }else{
            KafkaConsumerUtil kafkaConsumerUtil = new KafkaConsumerUtil(threadSwitch, seeds, Integer.parseInt(port), topic, "kafka-tool", consumerPartition, offset);
            threadPool.submit( new ConsumerTask(msgQueue, kafkaConsumerUtil) );
        }

        while ( threadSwitch.isStart() ){
            try {
                String msg = msgQueue.poll(1, TimeUnit.SECONDS);
                session.sendMessage(new TextMessage( msg ));
            }catch (Exception ignore){

            }
        }
        System.out.println("stop consumer ................shutdown ");
    }

    public void stopConsumer(){
        this.threadSwitch.setStart(false);
    }

    public void appendMsg(String brokers, String topic, String msg) {
        try {
            kafkaProducer = new KafkaProducer(brokers, topic);
            kafkaProducer.appendByKey(msg);
        }catch (Exception e){
            throw e;
        }finally {
            kafkaProducer.close();
        }
    }

    private Map<String,Object> getSeedsAndPort(String brokers) {
        Map<String,Object> res = new HashMap<>();
        List<String> hostList = new ArrayList<>();
        if( !StringUtils.isEmpty( brokers ) ){
            String[] brokerArr = StringUtils.split(brokers, ",");
            for(int i = 0; i < brokerArr.length; i++){
                String[] tmps = brokerArr[i].split(":");
                res.put("port", tmps[1]);
                hostList.add( tmps[0] );
            }
        }
        res.put("seeds", hostList);
        return res;
    }

    public List<ConsumerMonitor> getConsumerMonitorList() {
        List<ConsumerMonitor> listMonitor = new ArrayList<>();
        for(ConsumerTemplet consumerTemplet : ConsumerSchedule.consumerServiceList ){
            ConsumerMonitor consumerMonitor = new ConsumerMonitor();
            consumerMonitor.setZookeeper( consumerTemplet.getZookeeper() );
            consumerMonitor.setTopic( consumerTemplet.getTopic() );
            consumerMonitor.setConsumerNum( consumerTemplet.getConsumerNum() );
            consumerMonitor.setGroupId( consumerTemplet.getGroupId() );
            Long msgNum = consumerTemplet.getConsumerService().getMsgNum().longValue();
            consumerMonitor.setMsgNum( msgNum );
            listMonitor.add( consumerMonitor );
        }
        return listMonitor;
    }


    class ConsumerTask implements Runnable{
        private KafkaConsumerUtil kafkaConsumerUtil;
        private BlockingQueue<String> msgQueue;
        public ConsumerTask(BlockingQueue<String> msgQueue, KafkaConsumerUtil kafkaConsumerUtil){
            this.kafkaConsumerUtil = kafkaConsumerUtil;
            this.msgQueue = msgQueue;
        }
        @Override
        public void run() {
            kafkaConsumerUtil.execute(new ConsumerRunnable() {
                @Override
                public void consumer(long offset, String msg) {
                    try {
                        msgQueue.add(msg);
                    } catch (Exception ignore) {

                    }
                }
            });
        }
    }

}
