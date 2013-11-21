package p2pfs.filesystem.bridges.dht;

import java.io.IOException;
import p2pfs.filesystem.types.dto.ExceptionDTO;
import p2pfs.filesystem.types.dto.FileSystemDTO;
import p2pfs.filesystem.types.dto.GetDTO;
import p2pfs.filesystem.types.dto.PutDTO;

import net.tomp2p.peers.Number160;

/**
 * KademliaBridge is the abstract class representing the integration layer 
 * between the FUSE integration layer and the DHT.
 * This layer aims to abstract the two choices a user have:
 *  - be a member of the DHT;
 *  - don't be a member of the DHT and be connected to a member via a Socket.
 */
public class KademliaBridge {
	
	/**
	 * State of the bridge is either local (we have entered the DHT) or remote
	 *  (we are using another peer inside the DHT).
	 */
	private BridgeState state;
	
	/**
	 * Constructor.
	 * @param state - initial state.
	 */
	public KademliaBridge(BridgeState state) { this.state = state; }
	
	/**
	 * Get method to retrieve a value given a location key.
	 * @param locationKey - the location key.
	 * @return - the value.
	 * @throws IOException - any problem with the socket connection.
	 * @throws ClassNotFoundException - problems casting the readObject method.
	 * @throws Throwable - this throwable may come from the DHT (this exception
	 * is more general than the other two).
	 */
	public Object get(Number160 locationKey) throws Throwable { 
		// writing request.
		this.state.getPeerOOS().writeObject(new GetDTO(locationKey));
		this.state.getPeerOOS().flush();
		// waiting and reading the answer.
		FileSystemDTO fsDTO = (FileSystemDTO) this.state.getPeerOIS().readObject();
		// test for exception.
		if(fsDTO instanceof ExceptionDTO) {
			Throwable t = (Throwable) fsDTO.getObject();
			throw t;
		}
		// return the answered object.
		return fsDTO.getObject(); }
	
	/**
	 * Put method to store a value give a location key.
	 * @param locationKey - the location key.
	 * @param value - the value to be stored.
	 * @throws ClassNotFoundException - problems casting the readObject method.
	 * @throws Throwable - this throwable may come from the DHT (this exception
	 * is more general than the other two).
	 * @return boolean - if the operation was finished successfully or not.
	 */
	public void put(Number160 locationKey, Object value) throws Throwable {
		// writing request.
		this.state.getPeerOOS().writeObject(new PutDTO(locationKey, value));
		this.state.getPeerOOS().flush();
		// waiting and reading the answer.
		FileSystemDTO fsDTO = (FileSystemDTO) this.state.getPeerOIS().readObject();
		// test for exception.
		if(fsDTO instanceof ExceptionDTO) {
			Throwable t = (Throwable) fsDTO.getObject();
			throw t;
		}
	}
	
	/**
	 * Setter.
	 * @param state - the new state.
	 */
	protected void setState(BridgeState state) { this.state = state; }

}
