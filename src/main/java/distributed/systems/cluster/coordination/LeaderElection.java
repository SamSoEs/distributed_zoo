package distributed.systems.cluster.coordination;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class LeaderElection implements Watcher{
	private static final String ZOOKEEPER_ADDRESS = "localhost:2181";
	public static final int SESSION_TIMEOUT = 3000;
	private static final String ELECTION_NAMESPACE = "/election";
	private ZooKeeper zooKeeper;
	private String currentZnodeName;
	
	public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
		
		
		LeaderElection leaderElection = new LeaderElection();
		
		
		leaderElection.connectToZookeeper();
		leaderElection.volunteerForLeadership();
		leaderElection.reelectLeader();
		leaderElection.run();
		leaderElection.close();
	}
	
	public void volunteerForLeadership() throws KeeperException, InterruptedException {
		String zNodePrefix = ELECTION_NAMESPACE + "/c";
		String znodeFullPath = zooKeeper.create(zNodePrefix, new byte[] {}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		System.out.println("Zoo name " + znodeFullPath);
		this.currentZnodeName = znodeFullPath.replace(ELECTION_NAMESPACE + "/", "");
	}
	
	 public void reelectLeader() throws KeeperException, InterruptedException {
	        Stat predecessorStat = null;
	        String predecessorZnodeName = "";
	        while (predecessorStat == null) {
	            List<String> children = zooKeeper.getChildren(ELECTION_NAMESPACE, false);

	            Collections.sort(children);
	            String smallestChild = children.get(0);

	            if (smallestChild.equals(currentZnodeName)) {
	                System.out.println("I am the leader");
	                return;
	            } else {
	                System.out.println("I am not the leader");
	                int predecessorIndex = Collections.binarySearch(children, currentZnodeName) - 1;
	                predecessorZnodeName = children.get(predecessorIndex);
	                predecessorStat = zooKeeper.exists(ELECTION_NAMESPACE + "/" + predecessorZnodeName, this);
	            }
	        }

	        System.out.println("Watching znode " + predecessorZnodeName);
	        System.out.println();
	    }
	
	public void connectToZookeeper() throws IOException {
		this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, this);
	}
	
	public void run() throws InterruptedException {
		synchronized(zooKeeper) {
			zooKeeper.wait();
		}
	}
	
	public void close() throws InterruptedException {
		zooKeeper.close();
	}
	@Override
	public void process(WatchedEvent event) {
		 switch (event.getType()) {
         case None:
             if (event.getState() == Event.KeeperState.SyncConnected) {
                 System.out.println("Successfully connected to Zookeeper");
             } else {
                 synchronized (zooKeeper) {
                     System.out.println("Disconnected from Zookeeper event");
                     zooKeeper.notifyAll();
                 }
             }
             break;
         case NodeDeleted:
             try {
                 reelectLeader();
                 
             } catch (InterruptedException e) {
            	 
             } catch (KeeperException e) {
            	 
             }
     }
	}
}
