package p2pfs.filesystem.bridges.dht;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Bridge state represents the state of the bridge: local or remote.
 * (check KademliaBridge documentation for more info.)
 */
public abstract class BridgeState {
	
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
	public BridgeState() { }	
	
	/**
	 * The method that returns the socket to be used for the current request.
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
