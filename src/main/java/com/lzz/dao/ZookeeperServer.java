package com.lzz.dao;

import org.apache.curator.test.TestingServer;

import java.io.IOException;

/**
 *
 * @scrope testing
 *
 */
public class ZookeeperServer {
	
	private TestingServer testingServer;

	public void initZK() throws Exception {
		
		testingServer = new TestingServer();
		testingServer.start();
	}
	
	public String getHostPort() {
		return testingServer.getConnectString();
	}
	
	public void closeZK() throws IOException {
		testingServer.close();
	}
}
