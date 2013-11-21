package p2pfs.filesystem.bridges.dht;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
	 * Object streams. This streams should be used to write and read from 
	 * the socket.
	 */
	ObjectInputStream ois = null;
	ObjectOutputStream oos = null;
	
	/**
	 * Constructor.
	 */
	public BridgeState() 
	{ this.bridge = Main.getKademliaBridge(); }
	
	/**
	 * The method that returns the socket to be used for the current request.
	 * This method might be deleted in future since it is not needed (just for
	 * debug).
	 * @return - a client socket.
	 */
	public abstract Socket getPeerSocket();
	/**
	 * Methods that return the input and output streams.
	 * @return - object streams.
	 * @throws IOException  - due to the creation of the streams.
	 */
	public abstract ObjectInputStream getPeerOIS() throws IOException;
	public abstract ObjectOutputStream getPeerOOS() throws IOException;

}
