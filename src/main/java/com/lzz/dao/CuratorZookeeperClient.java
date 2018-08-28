package com.lzz.dao;

import com.lzz.util.IOUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.recipes.nodes.PersistentNode;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.PathUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.util.List;

/**
 *
 */
@Component
public class CuratorZookeeperClient implements ZookeeperClient {

	private CuratorFramework client;

	public CuratorZookeeperClient(){

	}

	public CuratorZookeeperClient(String hostPort, int sessionTimeout) {
		this(hostPort, sessionTimeout / 2, sessionTimeout);
	}

	/**
	 *
	 * @param hostPort
	 * @param connectionTimeout
	 * @param sessionTimeout
	 */
	public CuratorZookeeperClient(String hostPort, int connectionTimeout, int sessionTimeout){
		if( !checkHostPort(hostPort) ){
			return;
		}
		client = CuratorFrameworkFactory.builder().connectString(hostPort)
				.retryPolicy(new ExponentialBackoffRetry(1000, 3)).connectionTimeoutMs(connectionTimeout)
				.sessionTimeoutMs(sessionTimeout).build();
		client.start();
		try {
			client.blockUntilConnected();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private boolean checkHostPort(String hostPort) {
		boolean res = true;
		if( !org.apache.commons.lang3.StringUtils.isBlank( hostPort ) ){
			String[] tmpArr = hostPort.split(":");
			if( tmpArr.length < 2 ){
				res = false;
			}
		}else{
			res = false;
		}
		return res;
	}


	/**
	 *
	 */
	public void shutDown() {
		IOUtils.closeQuietly(client);
	}


	@Override
	public boolean createNode(String path, byte[] value) throws Exception {
		
		PathUtils.validatePath(path);
		return client.create().creatingParentsIfNeeded().forPath(path, value) != null;
	}
	
	@Override
	public boolean deleteNode(String path) throws Exception {
		
		PathUtils.validatePath(path);
		client.delete().deletingChildrenIfNeeded().forPath(path);
		return true;
	}
	
	@Override
	public void setData(String path, byte[] content) throws Exception {
		
		PathUtils.validatePath(path);
		client.setData().forPath(path, content);
	}
	
	@Override
	public byte[] getData(String path) throws Exception {
		
		PathUtils.validatePath(path);
		return client.getData().forPath(path);
	}

	@Override
	public Stat getDataStat(String path) throws Exception {
		return client.checkExists().forPath(path);
	}

	@Override
	public List<String> listChildrenNode(String path) throws Exception {
		
		PathUtils.validatePath(path);
		return client.getChildren().forPath(path);
	}
	
	@Override
	public boolean checkExists(String path) throws Exception {
		
		PathUtils.validatePath(path);
		return client.checkExists().forPath(path) != null;
	}
	
	@Override
	public void logoutEphemeralNode(Closeable node) throws Exception {
		if (node != null)
			node.close();
	}
	
	@Override
	public Closeable loginEphemeralNode(String path, byte[] data) throws Exception {
		PersistentNode persistentEphemeralNode = new PersistentNode(client,
				CreateMode.EPHEMERAL, false, path, data);
		persistentEphemeralNode.start();
//		persistentEphemeralNode.waitForInitialCreate(3000, TimeUnit.MILLISECONDS);
		return persistentEphemeralNode;
	}

	@Override
	public void addTreeChangedWatcher(String path, WatcherHandler watcherHandler) throws Exception {
		
		TreeCache treeCache = new TreeCache(client, path);
		treeCache.getListenable().addListener(new TreeCacheListener() {
			@Override
			public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
				watcherHandler.process(path);
			}
		});
		treeCache.start();
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				treeCache.close();
			}
		}));
	}
}

