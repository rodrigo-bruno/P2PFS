package p2pfs.filesystem.types.dto;

import net.tomp2p.futures.FutureDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;

/**
 * This class is the base class for all Data Transfers Objects that will be 
 * used to send requests via socket to the PeerThread thread.
 */
public abstract class FileSystemDTO {
	
	/**
	 * Location key to use in the operation.
	 */
	protected final Number160 locationKey;
	
	/**
	 * Constructor.
	 * @param locationKey
	 */
	public FileSystemDTO(Number160 locationKey)
	{ this.locationKey = locationKey; }
	
	/**
	 * Method to implement by subclasses.
	 * This method will receive the Peer in which the operation will be applied.
	 * @param peer - the dht peer.
	 * @return - a future object that will be used to hold the answer.
	 */
	public abstract FutureDHT execute(Peer peer);

}
