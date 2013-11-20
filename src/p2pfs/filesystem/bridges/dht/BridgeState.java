package p2pfs.filesystem.bridges.dht;

import java.net.Socket;

import p2pfs.filesystem.Main;

/**
 * Bridge state represents the state of the bridge: local or remote.
 * (check KademliaBridge documentation for more info.)
 */
public abstract class BridgeState {
	
	/**
	 * A reference to the actual bridge.
	 * This is here so that states can be exchanged by their own.
	 */
	protected KademliaBridge bridge = null;
	
	/**
	 * The socket that will be used to make requests to the thread that is 
	 * in the DHT.
	 */
	protected Socket socket = null;
	
	/**
	 * Constructor.
	 */
	public BridgeState() { this.bridge = Main.getKademliaBridge(); }
	
	/**
	 * The method that returns the socket to be used for the current request.
	 * @return - a client socket.
	 */
	public abstract Socket getPeerSocket();

}
