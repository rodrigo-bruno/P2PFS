package p2pfs.filesystem.bridges.dht;

import net.tomp2p.peers.Number160;

/**
 * LocalBridgeImpl means that this peer will have remote access to the DHT.
 * This class will communicate with a remote node via socket.
 * That node will do all requests and report back all answers.
 */
public class RemoteBridgeImpl implements KademliaBridge {

	@Override
	public Object get(Number160 locationKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void put(Number160 locationKey, Object value) {
		// TODO Auto-generated method stub
		
	}

}
