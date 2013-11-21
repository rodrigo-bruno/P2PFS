package p2pfs.filesystem.types.dto;

import java.io.ObjectOutputStream;
import p2pfs.filesystem.PeerThread;
import net.tomp2p.futures.FutureDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;

/**
 * DTO used to transfer a get operation.
 */
public class GetDTO extends RequestDTO {

	/**
	 * Serialization id. 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor. 
	 * @param locationKey
	 */
	public GetDTO(Number160 locationKey) 
	{ super(locationKey); }

	/**
	 * See base class documentation.
	 */
	@Override
	public FutureDHT execute(Peer peer, ObjectOutputStream oos) 
	{ return peer.
			get(this.getLocationKey()).
			start().
			addListener(new PeerThread.GetFuture(oos)); }

}