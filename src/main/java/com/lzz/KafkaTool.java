package com.lzz;

import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.utils.ZkUtils;
import org.apache.kafka.common.security.JaasUtils;

import java.util.Properties;

/**
 * Created by lzz on 2018/1/16.
 */
public class KafkaTool {
    public static void createTopic(String zk, String topic, int partition, int replication){
        ZkUtils zkUtils = ZkUtils.apply(zk, 30000, 30000, JaasUtils.isZkSecurityEnabled());
        AdminUtils.createTopic(zkUtils, topic, partition, replication, new Properties(), RackAwareMode.Enforced$.MODULE$);
        zkUtils.close();
    }


    public static void getBroders(String zk){
        //
    }


}
