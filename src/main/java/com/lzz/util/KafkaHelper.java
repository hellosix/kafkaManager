package com.lzz.util;

import com.lzz.dao.CuratorZookeeperClient;
import kafka.admin.AdminUtils;
import kafka.api.OffsetRequest;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.cluster.Broker;
import kafka.common.TopicAndPartition;
import kafka.javaapi.*;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.utils.ZKStringSerializer$;
import org.I0Itec.zkclient.ZkClient;

import java.util.*;

public class KafkaHelper {
    private static int SESSION_TIME_OUT_MS = 10 * 1000;
    private static int CONNECTION_TIME_OUT_MS = 8 * 1000;
    /**
     * 获取kafka logSize
     * @param topic
     * @param partition
     * @return
     */
    public static long getLogSize(String broker, String topic,int partition){
        String[] arr = broker.split(":");
        return getLogSize( arr[0], Integer.parseInt(arr[1]), topic, partition);
    }
    public static long getLogSize(String host,int port,String topic,int partition){
        long offset = 0;
        try {
            String clientName = "Client_" + topic + "_" + partition;
            Broker leaderBroker = getLeaderBroker(host, port, topic, partition);
            String reaHost = null;
            if (leaderBroker != null) {
                reaHost = leaderBroker.host();
            }else {
                System.out.println("Partition of Host is not find");
            }
            SimpleConsumer simpleConsumer = new SimpleConsumer(reaHost, port, 10000, 64*1024, clientName);
            TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);
            Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
            requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(OffsetRequest.LatestTime(), 1));
            kafka.javaapi.OffsetRequest request = new kafka.javaapi.OffsetRequest(requestInfo, OffsetRequest.CurrentVersion(), clientName);
            OffsetResponse response = simpleConsumer.getOffsetsBefore(request);
            if (response.hasError()) {
                System.out.println("Error fetching data Offset , Reason: " + response.errorCode(topic, partition) );
                return 0;
            }
            long[] offsets = response.offsets(topic, partition);
            offset = offsets[0];
        }catch (Exception e){

        }
        return offset;
    }

    /**
     * 获取broker ID
     * @param host
     * @param port
     * @param topic
     * @param partition
     * @return
     */
    public static Integer getBrokerId(String host,int port,String topic,int partition){
        Broker leaderBroker = getLeaderBroker(host, port, topic, partition);
        if (leaderBroker != null) {
            return leaderBroker.id();
        }
        return null;
    }
    /**
     * 获取leaderBroker
     * @param host
     * @param port
     * @param topic
     * @param partition
     * @return
     */
    private static Broker getLeaderBroker(String host, int port, String topic, int partition){
        String clientName = "Client_Leader_LookUp";
        SimpleConsumer consumer = null;
        PartitionMetadata partitionMetaData = null;
        try {
            consumer = new SimpleConsumer(host, port, 10000, 64*1024, clientName);
            List<String> topics = new ArrayList<String>();
            topics.add(topic);
            TopicMetadataRequest request = new TopicMetadataRequest(topics);
            TopicMetadataResponse reponse = consumer.send(request);
            List<TopicMetadata> topicMetadataList = reponse.topicsMetadata();
            for(TopicMetadata topicMetadata : topicMetadataList){
                for(PartitionMetadata metadata : topicMetadata.partitionsMetadata()){
                    if (metadata.partitionId() == partition) {
                        partitionMetaData = metadata;
                        break;
                    }
                }
            }
            if (partitionMetaData != null) {
                return partitionMetaData.leader();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void createTopic(String zk, String topic, int partition, int replication){
        ZkClient zkClient = null;
        try {
            zkClient = new ZkClient(
                    zk,
                    SESSION_TIME_OUT_MS,
                    CONNECTION_TIME_OUT_MS,
                    ZKStringSerializer$.MODULE$);
            Properties topicConfig = new Properties();
            AdminUtils.createTopic(zkClient, topic, partition, replication, topicConfig);
        }catch (Exception e){
            throw  e;
        }finally {
            if( null != zkClient ){
                zkClient.close();
            }
        }
    }

    public static void topicConfig(String zk, String topic){
        ZkClient zkClient = null;
        try {
            zkClient = new ZkClient(
                    zk,
                    SESSION_TIME_OUT_MS,
                    CONNECTION_TIME_OUT_MS,
                    ZKStringSerializer$.MODULE$);
            kafka.api.TopicMetadata topicMetadata =  AdminUtils.fetchTopicMetadataFromZk(topic, zkClient);
        }catch (Exception e){
            throw  e;
        }finally {
            if( null != zkClient ){
                zkClient.close();
            }
        }
    }

    public static void deleteTopic(String zk, String topic){
        ZkClient zkClient = null;
        try {
            zkClient = new ZkClient(
                    zk,
                    SESSION_TIME_OUT_MS,
                    CONNECTION_TIME_OUT_MS,
                    ZKStringSerializer$.MODULE$);
            AdminUtils.deleteTopic(zkClient, topic);
        }catch (Exception e){
            throw  e;
        }finally {
            if( null != zkClient ){
                zkClient.close();
            }
        }
    }
}