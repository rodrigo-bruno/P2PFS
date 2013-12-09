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
	public Directory getHomeDirectory(String username)
	{ return this.getHomeDirectory(this.constructHomeDirectoryID(username)); }
	
	/**
	 * Method to retrieve the user's Home Directory.
	 * @param key - the DHT key.
	 * @return - the user's home directory or null if it fails.
	 */
	public abstract Directory getHomeDirectory(Number160 key);
	
	/**
	 * Method to retrieve a file block.
	 * @param filePath - the path to the file that contains the block.
	 * @param blockNumber - the number of the block.
	 * @return - a byte buffer or null if it fails
	 */
	public ByteBuffer getFileBlock(String filePath, int blockNumber) 
	{ return this.getFileBlock(this.constructFileBlock(filePath, blockNumber)); }
	
	/**
	 * Method to retrieve a file block.
	 * @param key - the DHT key.
	 * @return - a byte buffer or null if it fails
	 */
	public abstract ByteBuffer getFileBlock(Number160 key);
	
	/**
	 * Method to store the user's Home Directory.
	 * @param username - the user name
	 * @param directory - the user's home directory.
	 * @param boolean - if the operation succeed or not.
	 */
	public boolean putHomeDirectory(String username, Directory directory) 
	{ return this.putHomeDirectory(this.constructHomeDirectoryID(username), directory); }

	/**
	 * Method to store the user's Home Directory.
	 * @param key - the DHT key.
	 * @param directory - the user's home directory.
	 * @param boolean - if the operation succeed or not.
	 */
	public abstract boolean putHomeDirectory(Number160 key, Directory directory);
	
	/**
	 * Method to retrieve a file block.
	 * @param filePath - the path to the file that contains the block.
	 * @param blockNumber - the number of the block.
	 * @param buffer - the buffer to store.
	 * @param boolean - if the operation succeed or not.
	 */
	public boolean putFileBlock(String filePath, int blockNumber, ByteBuffer buffer) 
	{ return this.putFileBlock(this.constructFileBlock(filePath, blockNumber), buffer); }

	/**
	 * Method to retrieve a file block.
	 * @param key - the DHT key.
	 * @param buffer - the buffer to store.
	 * @param boolean - if the operation succeed or not.
	 */
	public abstract boolean putFileBlock(Number160 key, ByteBuffer buffer);
	
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
