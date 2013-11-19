package p2pfs.filesystem.bridges.dht;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import p2pfs.filesystem.PeerThread;

/**
 * LocalBridgeState means that this node will have local access to the DHT.
 */
public class LocalBridgeState extends BridgeState {

	/**
	 * Constructor - creates a socket connection to the local peer thread.
	 * @param bridge 
	 * @throws UnknownHostException - if there is some problem finding the host.
	 * @throws IOException - any problem regarding the socket establishment.
	 */
	public LocalBridgeState(KademliaBridge bridge) 
			throws UnknownHostException, IOException {
		super(bridge);
		this.socket = new Socket("localhost", PeerThread.FILESYSTEM_PORT);
	}

	/**
	 * see base doc.
	 */
	@Override
	public Socket getPeerSocket() { return this.socket; }


}
