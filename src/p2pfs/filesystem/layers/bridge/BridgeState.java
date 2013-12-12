package p2pfs.filesystem.layers.bridge;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import p2pfs.filesystem.Main;
import p2pfs.filesystem.layers.host.PeerThread;

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
	 * TODO
	 * @return
	 */
	public Socket getNewPeerSocket() {
		Random rand = new Random();
		int numberBootsraps = Main.BOOTSTRAP_NODES.length;
		String tryAddr;
		while(true) {
			try {
				tryAddr = Main.BOOTSTRAP_NODES[rand.nextInt(numberBootsraps)];
				this.socket = new Socket(tryAddr, PeerThread.FILESYSTEM_PORT);
				break;
			}
			// this happens if the bootstrap node is down.
			catch (ConnectException e ) {} 
			catch (UnknownHostException e) { } 
			catch (IOException e) { }			
		}
		System.out.println("Connection established to " + tryAddr);
		return this.socket;
	}
	
	/**
	 * Methods that return the input and output streams.
	 * @return - object streams.
	 * @throws IOException  - due to the creation of the streams.
	 */
	public abstract ObjectInputStream getPeerOIS() throws IOException;
	public abstract ObjectOutputStream getPeerOOS() throws IOException;

}
