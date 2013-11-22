package p2pfs.filesystem.bridges.dht;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import p2pfs.filesystem.PeerThread;

/**
 * RemoteBridgeState means that this node will not have local access to the DHT.
 * This class will try to connect to the closest node which has the username
 * info stored.
 * It will also check if it is time to change to a LocalBridgeState. If it is the
 * case, it will perform the change.
 */
public class RemoteBridgeState extends BridgeState {

	/**
	 * Constructor - creates a socket connection to the remote peer thread.
	 * @throws UnknownHostException - if there is some problem finding the host.
	 * @throws IOException - any problem regarding the socket establishment.
	 */
	public RemoteBridgeState(final String[] bootstrapNodes) 
			throws UnknownHostException, IOException {
		// TODO: get a random bootstrapNode and try others if the first one fails.
		// TODO: try to connect to the node with the user name.
		this.socket = new Socket(bootstrapNodes[0], PeerThread.FILESYSTEM_PORT); 
	}

	/**
	 * see base doc.
	 */
	@Override
	public Socket getPeerSocket() { return this.socket; }

	// TODO: check time to change state!
	/**
	 * see base doc.
	 */
	@Override
	public ObjectInputStream getPeerOIS() throws IOException {
		if (this.ois == null)
		{ this.ois = new ObjectInputStream(this.socket.getInputStream()); }
		return this.ois;
	}

	/**
	 * see base doc.
	 */
	@Override
	public ObjectOutputStream getPeerOOS() throws IOException {
		if (this.oos == null)
		{ this.oos = new ObjectOutputStream(this.socket.getOutputStream()); }
		return this.oos;
	}


}
