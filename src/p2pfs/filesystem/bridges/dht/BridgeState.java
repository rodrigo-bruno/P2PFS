package p2pfs.filesystem.bridges.dht;

import java.net.Socket;

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
	 * @param bridge - the actual bridge.
	 */
	public BridgeState(KademliaBridge bridge) { this.bridge = bridge; }
	
	/**
	 * The method that returns the socket to be used for the current request.
	 * @return - a client socket.
	 */
	public abstract Socket getPeerSocket();

}
