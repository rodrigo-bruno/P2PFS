package p2pfs.filesystem;

import java.io.IOException;
import java.util.Random;

import p2pfs.filesystem.bridges.dht.KademliaBridge;
import p2pfs.filesystem.bridges.dht.LocalBridgeState;

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
	 * 
	 */
	private static KademliaBridge KADEMLIA_BRIDGE = null;
	
	/**
	 * The user id for the one trying to mount the file system.
	 */
	public static String USERNAME = null;
	
	/**
	 * The place where the file system will be mounted.
	 */
	public static String MOUNTPOINT = null;

	/**
	 * Main method.
	 * @param args - accepted scenarios: {} | {username mountpoint}
	 * @throws IOException - if something happens during the initPeerThread method.
	 */
	public static void main(String[] args) throws IOException {
		// means that we need to mount the FS
		if(args.length == 2) {
			Main.USERNAME = args[0];
			Main.MOUNTPOINT = args[1];
			// TODO - implement the RemoteBridgeState and complete this code.
			System.out.println("WARNING: not implemented yet.");
		} else if(args.length == 1) {
			System.out.println("Wrong arguments! Possible scenarios:");
			System.out.println("java -jar p2pfs.jar (will only host files)");
			System.out.println(
					"java -jar p2pfs.jar <username> <mountpoint> " +
					"(will mount the P2PFS on the local FS and host files");
		} else {
			Main.initPeerThread();
			Main.KADEMLIA_BRIDGE = new KademliaBridge(new LocalBridgeState());
		}
		// Shutdown mechanism and protection.
		Runtime.getRuntime().addShutdownHook(new Thread() {
			/**
			 * The method responsible for cleaning all threads and sockets.
			 */
			@Override
			public void run() { 
				if(Main.PEER_THREAD != null) { Main.PEER_THREAD.interrupt(); }
				// TODO: check if it is necessary to close the KademliaBridge socket.
				try { Main.PEER_THREAD.join(); }
				// this exception will hardly happen (if we receive an interrupt
				// exception while performing the join).
				catch (InterruptedException e) { e.printStackTrace(); }
			}
		});
	}
	
	/**
	 * Method that will initialize the PeerThread thread.
	 * It will decide the peer id based on the fact that we are running a client
	 * side or a host side only. 
	 * @throws IOException - if some problem happens when creating sockets, 
	 * connecting to the DTH, ...
	 */
	public static void initPeerThread() throws IOException {
		// Using the username as the key for the peer and its own files is going
		// to be more efficient when accessing to data!
		if(Main.USERNAME != null)
		{ Main.PEER_THREAD = new PeerThread(Number160.createHash(Main.USERNAME)); }
		// For the ones only hosting files a random id does the job.
		else
		{ Main.PEER_THREAD = new PeerThread(new Number160(new Random())); }
	}
	
	/**
	 * Getters.
	 */
	public static PeerThread getPeerThread() { return Main.PEER_THREAD; }
	public static KademliaBridge getKademliaBridge() { return Main.KADEMLIA_BRIDGE; }
}
