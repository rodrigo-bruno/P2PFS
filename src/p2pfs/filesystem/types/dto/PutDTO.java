package p2pfs.filesystem.types.dto;

import net.tomp2p.futures.FutureDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;

/**
 * DTO used to transfer a put operation.
 */
public class PutDTO extends FileSystemDTO {

	/**
	 * Constructor. 
	 * @param locationKey
	 */
	public PutDTO(Number160 locationKey) 
	{ super(locationKey); }

	/**
	 * See base class documentation.
	 */
	@Override
	public FutureDHT execute(Peer peer) 
	{ return peer.get(this.locationKey).start(); }

}
