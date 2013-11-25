package p2pfs.filesystem.types.dto;

import java.io.IOException;
import java.io.ObjectOutputStream;
import p2pfs.filesystem.layers.host.*;
import net.tomp2p.futures.FutureDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

/**
 * DTO used to transfer a put operation.
 */
public class PutDTO extends RequestDTO {

	/**
	 * Serialization id. 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Object that will be stored.
	 */
	private Object value;
	
	/**
	 * Constructor. 
	 * @param locationKey
	 */
	public PutDTO(Number160 locationKey, Object value) { 
		super(locationKey);
		this.value = value;
	}

	/**
	 * See base class documentation.
	 * @throws IOException - might be thrown when Data object is created.
	 */
	@Override
	public FutureDHT execute(Peer peer, ObjectOutputStream oos) throws IOException 
	{ return peer.
			put(this.getLocationKey()).
			setData(new Data(this.value)).
			start().
			addListener(new PeerThread.PutFuture(oos)); }

}
