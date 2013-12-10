package p2pfs.filesystem.layers.bridge;

import java.io.IOException;
import p2pfs.filesystem.types.dto.ExceptionDTO;
import p2pfs.filesystem.types.dto.FileSystemDTO;
import p2pfs.filesystem.types.dto.GetDTO;
import p2pfs.filesystem.types.dto.OperationCompleteDTO;
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
	 * This method is synchronized since we might need to change to used socket
	 * and we don't want to do it in the middle of an operation.
	 * @param locationKey - the location key.
	 * @return - the value or null if it fails.
	 * @throws IOException - any problem with the socket connection.
	 * @throws ClassNotFoundException - problems casting the readObject method.
	 * @throws Throwable - this throwable may come from the DHT (this exception
	 * is more general than the other two).
	 */
	public Object get(Number160 locationKey) throws Throwable {
		synchronized(this) {
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
			// test if the operation was successful 
			if(!((OperationCompleteDTO)fsDTO).getStatus()) { return null; }
			// return the answered object.
			return fsDTO.getObject(); }
	}
	
	/**
	 * Put method to store a value give a location key.
	 * This method is synchronized since we might need to change to used socket
	 * and we don't want to do it in the middle of an operation.
	 * @param locationKey - the location key.
	 * @param value - the value to be stored.
	 * @throws ClassNotFoundException - problems casting the readObject method.
	 * @throws Throwable - this throwable may come from the DHT (this exception
	 * is more general than the other two).
	 * @return boolean - if the operation was finished successfully or not.
	 */
	public boolean put(Number160 locationKey, Object value) throws Throwable {
		// TODO: if java.net.SocketException, pedir ao state outro socket
		synchronized(this) {
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
			return ((OperationCompleteDTO) fsDTO).getStatus();
		}
	}
	
	/**
	 * Setter.
	 * This method is synchronized since we might need to change to used socket
	 * and we don't want to do it in the middle of an operation.
	 * @param state - the new state.
	 */
	public void setState(BridgeState state) 
	{ synchronized(this) { this.state = state; } }
	
	/**
	 * Getter.
	 */
	public BridgeState getBridgeState() 
	{ return this.state; }

}
