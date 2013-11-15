package p2pfs.filesystem.bridges.dht;

import net.tomp2p.peers.Number160;

/**
 * KademliaBridge is the abstract class representing the integration layer 
 * between the FUSE integration layer and the DHT.
 * This layer aims to abstract the two choices a user have:
 *  - be a member of the DHT;
 *  - don't be a member of the DHT and be connected to a member via a Socket.
 */
public interface KademliaBridge {
	
	/**
	 * Get method to retrieve a value given a location key.
	 * @param locationKey - the location key.
	 * @return - the value.
	 */
	public Object get(Number160 locationKey);
	
	/**
	 * Put method to store a value give a location key.
	 * @param locationKey - the location key.
	 * @param value - the value to be stored.
	 */
	public void put(Number160 locationKey, Object value);

}
