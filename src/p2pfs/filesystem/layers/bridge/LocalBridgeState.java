package p2pfs.filesystem.layers.bridge;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import p2pfs.filesystem.layers.host.*;;

/**
 * LocalBridgeState means that this node will have local access to the DHT.
 */
public class LocalBridgeState extends BridgeState {

	/**
	 * Constructor - creates a socket connection to the local peer thread.
	 * @throws UnknownHostException - if there is some problem finding the host.
	 * @throws IOException - any problem regarding the socket establishment.
	 */
	public LocalBridgeState() throws UnknownHostException, IOException
	{ this.socket = new Socket("localhost", PeerThread.FILESYSTEM_PORT); }

	/**
	 * see base doc.
	 */
	@Override
	public Socket getPeerSocket() { return this.socket; }

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
