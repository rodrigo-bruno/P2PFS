package p2pfs.filesystem.layers.cache;

import java.nio.ByteBuffer;
import p2pfs.filesystem.layers.bridge.KademliaBridge;
import p2pfs.filesystem.types.fs.Directory;

/**
 * SimpleBridgeImpl means that the file system API implementation has no 
 * caching mechanism.
 * This class will issue requests directly to the DHT integration layer.
 */
public class SimpleBridgeImpl extends FileSystemBridge {

	/**
	 * Constructor.
	 * @param dht - see base doc.
	 */
	public SimpleBridgeImpl(KademliaBridge dht) { super(dht); }

	/**
	 * see base doc.
	 */
	@Override
	public Directory getHomeDirectory(String username) {
		Directory dir = null;
		try { 
			Object o = this.dht.get(this.constructHomeDirectoryID(username));
			if(o != null) { dir = (Directory) o; }
		}
		// This should not happen.
		catch (Throwable e) { e.printStackTrace(); }
		return dir;
	}

	/**
	 * see base doc.
	 */
	@Override
	public ByteBuffer getFileBlock(String filePath, int blockNumber) {
		ByteBuffer bb = null;
		try { 
			Object o = this.dht.get(this.constructFileBlock(filePath, blockNumber));
			if(o != null) { bb = (ByteBuffer) o; }
		}
		// This should not happen.
		catch (Throwable e) { e.printStackTrace(); }
		return bb;		
	}

	/**
	 * see base doc.
	 */
	@Override
	public boolean putHomeDirectory(String username, Directory directory) {
		boolean b = false;
		try { b = this.dht.put(this.constructHomeDirectoryID(username), directory);	}
		// This should not happen.
		catch (Throwable e) { e.printStackTrace(); }
		return b;
	}

	/**
	 * see base doc.
	 */
	@Override
	public boolean putFileBlock(String filePath, int blockNumber, ByteBuffer buffer) {
		boolean b = false;
		try { b= this.dht.put(this.constructFileBlock(filePath, blockNumber), buffer); }
		// This should not happen.
		catch (Throwable e) { e.printStackTrace(); }
		return b;
	}

}
