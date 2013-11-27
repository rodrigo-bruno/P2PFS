package p2pfs.filesystem.layers.cache;

import java.nio.ByteBuffer;

import net.tomp2p.peers.Number160;

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
	 * @return - the user's home directory or null if it fails.
	 */
	public abstract Directory getHomeDirectory(String username);
	
	/**
	 * Method to retrieve a file block.
	 * @param filePath - the path to the file that contains the block.
	 * @param blockNumber - the number of the block or null if it fails.
	 * @return
	 */
	public abstract ByteBuffer getFileBlock(String filePath, int blockNumber);
	
	/**
	 * Method to store the user's Home Directory.
	 * @param username - the user name
	 * @param directory - the user's home directory.
	 * @param boolean - if the operation succeed or not.
	 */
	public abstract boolean putHomeDirectory(String username, Directory directory);
	
	/**
	 * Method to retrieve a file block.
	 * @param filePath - the path to the file that contains the block.
	 * @param blockNumber - the number of the block.
	 * @param buffer - the buffer to store.
	 * @param boolean - if the operation succeed or not.
	 */
	public abstract boolean putFileBlock(String filePath, int blockNumber, ByteBuffer buffer);
	
	/**
	 * Method to get the key to be used inside the DHT.
	 * @param username
	 * @return
	 */
	protected Number160 constructHomeDirectoryID(String username) 
	{ return Number160.createHash(username); }
	
	/**
	 * Method to get the key to be used inside the DHT.
	 * @param filePath
	 * @param blockNumber
	 * @return
	 */
	protected Number160 constructFileBlock(String filePath, int blockNumber) 
	{ return Number160.createHash(filePath + "%" + blockNumber); }

}
