package com.lzz.controller;

import com.lzz.logic.ToolService;
import com.lzz.model.Broker;
import com.lzz.model.Consumer;
import com.lzz.model.ConsumerMonitor;
import com.lzz.model.Response;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by lzz on 2018/1/14.
 */
@Controller
public class ToolController {
    @Resource
    ToolService toolService;

    @RequestMapping("/client")
    public String client(Model model) {
        return "client";
    }

    @RequestMapping("/monitor")
    public String monitor(Model model) {
        return "monitor";
    }

    @RequestMapping("/consumer")
    public String consumer(Model model) {
        return "consumer";
    }

    @RequestMapping( value = "/topic-list", method = RequestMethod.GET)
    @ResponseBody
    public Response topicList(@RequestParam String zk) {
        if(StringUtils.isBlank( zk )){
            return Response.fail();
        }
        List<com.lzz.model.Topic> list =  toolService.getTopicList(zk);
        return Response.res( list );
    }

    @RequestMapping( value = "/broker-list", method = RequestMethod.GET)
    @ResponseBody
    public Response getBrokerList(@RequestParam String zk) {
        List<Broker> list = new ArrayList<>();
        try {
            list =  toolService.getBrokerList(zk);
        }catch (Exception e){
            Response.fail();
        }
        return Response.res( list );
    }

    @RequestMapping( value = "/consumer-list", method = RequestMethod.GET )
    @ResponseBody
    public Response getConsumerList(){
        List<ConsumerMonitor> consumerMonitorList = toolService.getConsumerMonitorList();
        return Response.res( consumerMonitorList );
    }

    @RequestMapping( value = "/consumer-groups", method = RequestMethod.GET)
    @ResponseBody
    public Response consumerGroups(@RequestParam String zk) {
        List<Consumer> list = new ArrayList<>();
        try {
            list =  toolService.getConsumerGroups(zk);
        }catch (Exception e){
            return Response.fail();
        }
        return Response.res( list );
    }

    @RequestMapping( value = "/consumer-detail", method = RequestMethod.POST)
    @ResponseBody
    public Response consumerDetail(@RequestBody JSONObject requestBody) {
        List<Map<String,String>> consumerList;
        try {
            int partitions = requestBody.getInt("partitions");
            String consumer = requestBody.getString("consumer");
            String topic = requestBody.getString("topic");
            String zk = requestBody.getString("zk");
            String broker = requestBody.getString("broker");
            consumerList = toolService.getConsumerDetail(zk, broker, topic, consumer, partitions);
        }catch (Exception e){
            return Response.fail( e.getMessage() );
        }
        return Response.res( consumerList );
    }


    @RequestMapping( value = "/create-topic", method = RequestMethod.POST)
    @ResponseBody
    public Response createTopic(@RequestBody JSONObject requestBody) {
        try {
            toolService.createTopic(requestBody);
        }catch (Exception e){
            return Response.fail( e.getMessage() );
        }
        return Response.success();
    }

    @RequestMapping( value = "/delete-topic", method = RequestMethod.GET)
    @ResponseBody
    public Response deleteTopic(@RequestParam String zk, @RequestParam String topic) {
        try {
            toolService.deleteTopic(zk, topic);
        }catch (Exception e){
            return Response.fail( e.getMessage() );
        }
        return Response.success();
    }


    @RequestMapping( value = "/producer-msg", method = RequestMethod.POST)
    @ResponseBody
    public Response producerMsg(@RequestBody JSONObject requestBody) {
        try {
            String topic = requestBody.getString("topic");
            String msg = requestBody.getString("msg");
            String brokers = requestBody.getString("brokers");
            toolService.appendMsg(brokers, topic, msg);
        }catch (Exception e){
            return Response.fail( e.getMessage() );
        }
        return Response.success();
    }

    @RequestMapping("/test")
    @ResponseBody
    public String tool(@RequestParam(value="name", defaultValue="World") String name) {
        return "hello " + name;
    }
}
