package org.apache.zookeeper.server;

import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ZooKeeperLocal {

	ZooKeeperServerMain zooKeeperServer;

	Thread runningThread = null;



	public ZooKeeperLocal(Properties zkProperties) throws FileNotFoundException, IOException {
		QuorumPeerConfig quorumConfiguration = new QuorumPeerConfig();
		try {
		    quorumConfiguration.parseProperties(zkProperties);
		} catch(Exception e) {
		    throw new RuntimeException(e);
		}
 
		zooKeeperServer = new ZooKeeperServerMain();
		final ServerConfig configuration = new ServerConfig();
		configuration.readFrom(quorumConfiguration);
		
		
		runningThread = new Thread() {
		    public void run() {
		        try {
		            zooKeeperServer.runFromConfig(configuration);
		        } catch (IOException e) {
		            System.out.println("ZooKeeper Failed");
		            e.printStackTrace(System.err);
		        }
		    }
		};

		runningThread.start();
	}


	public void stop() {
		zooKeeperServer.shutdown();
		runningThread.stop();
	}
}