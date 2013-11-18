package p2pfs.filesystem.types.dto;

import java.net.Socket;

import net.tomp2p.futures.FutureDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;

/**
 * This class is the base class for all Data Transfers Objects that will be 
 * used to send requests via socket to the PeerThread thread.
 */
public abstract class RequestDTO extends FileSystemDTO{
	
	/**
	 * Constructor.
	 * @param locationKey
	 */
	public RequestDTO(Number160 locationKey)
	{ super(locationKey); }
	
	/**
	 * Method to implement by subclasses.
	 * This method will receive the Peer in which the operation will be applied.
	 * @param peer - the dht peer.
	 * @param socket - where the answer will be written.
	 * @return - a future object that will be used to hold the answer.
	 */
	public abstract FutureDHT execute(Peer peer, Socket socket);
	
	/**
	 * Getter.
	 * @return the location key.
	 */
	public Number160 getLocationKey()
	{ return (Number160) this.object; }

}
