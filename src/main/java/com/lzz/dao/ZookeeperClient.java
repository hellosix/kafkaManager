package com.lzz.dao;

import org.apache.zookeeper.data.Stat;

import java.io.Closeable;
import java.util.List;

/**
 *
 */
public interface ZookeeperClient {
    void shutDown();

    /**
     * check the node is existed whether or not
     *
     * @param path
     * @return
     * @throws Exception
     */
    boolean checkExists(String path) throws Exception;

    /**
     * create the node according to the path
     *
     * @param path
     * @return
     * @throws Exception
     */
    boolean createNode(String path, byte[] value) throws Exception;

    /**
     * delete the node according to the path
     *
     * @param path
     * @return
     * @throws Exception
     */
    boolean deleteNode(String path) throws Exception;

    /**
     * set data to the node according to path
     *
     * @param path
     * @param content
     * @return
     * @throws Exception
     */
    void setData(String path, byte[] content) throws Exception;

    /**
     * get data from the node according to the path
     * @param path
     * @return
     * @throws Exception
     */
    byte[] getData(String path) throws Exception;

    Stat getDataStat(String path) throws Exception;

    /**
     * get list of children node from the path for node
     *
     * @param path
     * @return
     * @throws Exception
     */
    List<String> listChildrenNode(String path) throws Exception;

   
    /**
     * register an ephemeral node
     *
     * @param path
     * @return
     * @throws Exception
     */
    Closeable loginEphemeralNode(String path, byte[] data) throws Exception;
    
    /**
     * 
     * @param node
     * @throws Exception
     */
    void logoutEphemeralNode(Closeable node) throws Exception;
    
    /**
     * 
     * @param path
     * @param watcherHandler
     * @throws Exception
     */
    void addTreeChangedWatcher(String path, WatcherHandler watcherHandler) throws Exception;
    /**
     *
     */
    interface WatcherHandler {
    	
        void process(String path);
    }
}
