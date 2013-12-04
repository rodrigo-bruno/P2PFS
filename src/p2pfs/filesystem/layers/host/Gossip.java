package p2pfs.filesystem.layers.host;

import net.tomp2p.peers.Number160;

public class Gossip {

	private PeerThread peerThread;
	private int id;
	
	public Gossip(PeerThread peerThread, Number160 id) {
		this.peerThread = peerThread;
	}	
}
