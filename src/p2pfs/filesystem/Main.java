package p2pfs.filesystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import p2pfs.filesystem.layers.bridge.KademliaBridge;
import p2pfs.filesystem.layers.bridge.LocalBridgeState;
import p2pfs.filesystem.layers.bridge.RemoteBridgeState;
import p2pfs.filesystem.layers.cache.CachedBridgeImpl;
import p2pfs.filesystem.layers.cache.FileSystemBridge;
import p2pfs.filesystem.layers.fuse.Fuse;
import p2pfs.filesystem.layers.host.Gossip;
import p2pfs.filesystem.layers.host.PeerThread;

import net.tomp2p.peers.Number160;

/**
 * Main class.
 */
public class Main {
	
	/**
	 * The thread playing the host side. Check it's documentation for more info.
	 */
	private static PeerThread PEER_THREAD = null;
	
	/** 
	 * Reference to the gossip object 
	 */
	private static Gossip GOSSIP = null;
	
	/**
	 * Kademlia Bridge implementation. This object will be used to abstract the
	 * socket access to the peer thread.
	 */
	private static KademliaBridge KADEMLIA_BRIDGE = null;
	
	/**
	 * TODO
	 */
	private static FileSystemBridge FS_BRIDGE = null;
	
	/**
	 * TODO
	 */
	@SuppressWarnings("unused")
	private static Fuse FUSE = null;
	
	/**
	 * The user id for the one trying to mount the file system.
	 */
	public static String USERNAME = null;
	
	/**
	 * The place where the file system will be mounted.
	 */
	public static String MOUNTPOINT = null;
	
	/**
	 * Arrays of addresses for the bootstraping nodes.
	 * FIXME: this should be loaded from a config file.
	 */
	final public static String[] BOOTSTRAP_NODES = {
		"planetlab-1.tagus.ist.utl.pt", 
		"planetlab-2.tagus.ist.utl.pt"
	};
	//final public static String[] BOOTSTRAP_NODES = {"127.0.0.1"};
	
	/**
	 * Time in milliseconds until a node using a remote bridge state changes to 
	 * a local one.
	 * FIXME: this should be loaded from a config file.
	 */
	final private static int remoteStateTime = Integer.MAX_VALUE; 

	/**
	 * Main method.
	 * @param args - accepted scenarios: [username mountpoint]+
	 * @throws IOException - if something happens during the initPeerThread method.
	 */
	public static void main(String[] args) throws IOException {
		try{
			// means that we need to mount the FS
			if(args.length == 2) {
				Main.USERNAME = args[0];
				Main.MOUNTPOINT = args[1];
				Thread t = Main.getBridgeStateThread();
				t.setDaemon(true);
				t.start();
				System.out.println("Starting Bridge State Thread");

				Main.KADEMLIA_BRIDGE = new KademliaBridge(new RemoteBridgeState());
				System.out.println("Init Kademlia Bridge -> Done");
				//Main.FS_BRIDGE = new SimpleBridgeImpl(Main.KADEMLIA_BRIDGE);
				Main.FS_BRIDGE = new CachedBridgeImpl(Main.KADEMLIA_BRIDGE);
				System.out.println("Init FS Bridge -> Done");
				Main.FUSE = new Fuse(Main.FS_BRIDGE, Main.USERNAME, Main.MOUNTPOINT);
				System.out.println("Init FUSE -> Done");
			} 
			// means that we are only hosting files
			else if(args.length == 0) {
				System.out.println("Starting Initialization Process");
				Main.initPeerThread();
				System.out.println("Init Peer Thread -> Done");

				Main.KADEMLIA_BRIDGE = new KademliaBridge(new LocalBridgeState());
				System.out.println("Init Kademlia Bridge -> Done");			
			} 
			// error case
			else {
				System.out.println("Wrong arguments!");
				System.out.println("java -jar p2pfs.jar [username mountpoint]+");
			}

			// Shutdown mechanism and protection.
			Runtime.getRuntime().addShutdownHook(Main.getShutdownThread());

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			while(true) {
				try	{
					
					String line = br.readLine();
					// ADD REMOVE USERS
					if(line.length() > 5 && line.substring(0,5).equals("users")){		
						GOSSIP.updateNumUsers(Integer.parseInt(line.substring(6,line.length())));					
						//System.out.println(Integer.parseInt(line.substring(6,line.length())));
					}
					// ADD REMOVE ACTIVE USERS
					if(line.length() > 5 && line.substring(0,5).equals("activ")){		
						GOSSIP.updateNumActive(Integer.parseInt(line.substring(6,line.length())));					
						//System.out.println(Integer.parseInt(line.substring(6,line.length())));
					}
					// ADD REMOVE FILES
					else if(line.length() > 5 && line.substring(0,5).equals("files")){		
						GOSSIP.updateNumFiles(Integer.parseInt(line.substring(6,line.length())));					
					}
					// ADD REMOVE MB
					else if(line.length() > 5 && line.substring(0,5).equals("megab")){		
						GOSSIP.updateNumMB(Integer.parseInt(line.substring(6,line.length())));					
					}
					// SHOW GOSSIP
					else if(line.equals("gossip")){
						GOSSIP.showGossip();					
					}
					// SHOW GOSSIP
					else if(line.equals("self")){
						GOSSIP.showSelf();					
					}
					// SHOW PEERS
					else if(line.equals("peers")){	
						GOSSIP.showPeers();
					}
				} 
				catch (IOException e) {  e.printStackTrace(); }
			}
		}

		catch(IOException e) { throw e; }
		// some exception might be thrown from the host side (put operation)
		catch (Throwable e) { e.printStackTrace(); }
		finally { Main.getShutdownThread().start(); }
	}
	
	/**
	 * Method that will initialize the PeerThread thread.
	 * It will decide the peer id based on the fact that we are running a client
	 * side or a host side only. 
	 * @throws IOException - if some problem happens when creating sockets, 
	 * connecting to the DTH, ...
	 */
	public static void initPeerThread() throws IOException {
		Main.PEER_THREAD = new PeerThread(new Number160(new Random())); 
		System.out.println("Peer Thread Creation -> Done");
		Main.PEER_THREAD.start();
		
		if(Main.USERNAME != null) { Main.KADEMLIA_BRIDGE.setState(new LocalBridgeState()); }

		/* Gossip threads started here */
        Main.GOSSIP = new Gossip(Main.PEER_THREAD);
	}
	
	/**
	 * Method that returns the thread responsible for cleaning all threads.
	 * @return
	 */
	public static Thread getShutdownThread() {
		return new Thread() {
			/**
			 * The method responsible for cleaning all threads and mounts.
			 */
			@Override
			public void run() { 
				try { 
					Main.KADEMLIA_BRIDGE.getBridgeState().getPeerSocket().close();
					if(Main.PEER_THREAD != null) { 
						Main.PEER_THREAD.interrupt();
						Main.PEER_THREAD.join();
					} 
				}
				// this exception will hardly happen (if we receive an interrupt
				// exception while performing the join).
				catch (InterruptedException e) { e.printStackTrace(); }
				// if any problem happens during the Kademlia Bridge socket closing.
				catch (IOException e) {	e.printStackTrace(); } 
			}
		};
	}
	
	/**
	 * Thread that is counting the time until we change the remote bridge state
	 * to local (joining the DHT).
	 * @return
	 */
	public static Thread getBridgeStateThread() {
		return new Thread(new Runnable() {
			
			/**
			 * Method that does the job.
			 * After sleeping it starts by initializing the Peer Thread and then
			 * setting the Kademlia Bridge to local state.
			 */
			@Override
			public void run() {
				try {
					Thread.sleep(Main.remoteStateTime);
					Main.initPeerThread();
					Main.KADEMLIA_BRIDGE.setState(new LocalBridgeState());
				} 
				// if the thread is interrupted while sleeping.
				catch (InterruptedException e) { e.printStackTrace(); }
				// if something happens during the peer thread initialization.
				catch (IOException e) { e.printStackTrace(); }
				
			}	
		});
	}
}
