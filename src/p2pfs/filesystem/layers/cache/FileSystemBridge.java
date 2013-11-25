package p2pfs.filesystem.layers.cache;

import java.nio.ByteBuffer;

import p2pfs.filesystem.layers.bridge.KademliaBridge;
import p2pfs.filesystem.types.fs.Directory;

/**
 * FileSystemBridge is the abstract class representing the integration layer 
 * between the FUSE and the DHT integration layer.
 * This layer aims to abstract the two choices a user have:
 *  - use a simple file system implementation;
 *  - use a cached file system. 
 */
public abstract class FileSystemBridge {

	/**
	 * The bridge to access the DHT.
	 * This object will be used to get and put objects to the DHT.
	 * It abstracts the underlying layers.
	 */
	protected final KademliaBridge dht;
	
	/**
	 * Constructor.
	 * @param dht - the bridge to access the DHT.
	 */
	public FileSystemBridge(KademliaBridge dht) { this.dht = dht; }
	
	/**
	 * Method to retrieve the user's Home Directory.
	 * @param username - the user name
	 * @return - the user's home directory.
	 */
	public abstract Directory getHomeDirectory(String username);
	
	/**
	 * Method to retrieve a file block.
	 * @param filePath - the path to the file that contains the block.
	 * @param blockNumber - the number of the block.
	 * @return
	 */
	public abstract ByteBuffer getFileBlock(String filePath, int blockNumber);
	
	/**
	 * Method to store the user's Home Directory.
	 * @param username - the user name
	 * @param directory - the user's home directory.
	 */
	public abstract void putHomeDirectory(String username, Directory directory);
	
	/**
	 * Method to retrieve a file block.
	 * @param filePath - the path to the file that contains the block.
	 * @param blockNumber - the number of the block.
	 * @param buffer - the buffer to store.
	 */
	public abstract void putFileBlock(String filePath, int blockNumber, ByteBuffer buffer);

}
