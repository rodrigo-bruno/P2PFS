package p2pfs.filesystem.layers.cache;

import java.nio.ByteBuffer;

import net.tomp2p.peers.Number160;
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
	public Directory getHomeDirectory(Number160 key) {
		Directory dir = null;
		try { 
			Object o = this.dht.get(key);
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
	public ByteBuffer getFileBlock(Number160 key) {
		ByteBuffer bb = null;
		try 
		{ bb = ByteBuffer.wrap((byte[])this.dht.get(key)); }		
		// This should not happen.
		catch (Throwable e) { e.printStackTrace(); }	
		return bb;		
	}

	/**
	 * see base doc.
	 */
	@Override
	public boolean putHomeDirectory(Number160 key, Directory directory) {
		boolean b = false;
		try { b = this.dht.put(key, directory);	}
		// This should not happen.
		catch (Throwable e) { e.printStackTrace(); }
		return b;
	}

	/**
	 * see base doc.
	 */
	@Override
	public boolean putFileBlock(Number160 key, ByteBuffer bb) {
		boolean b = false;
		try 
		{ b = this.dht.put(key, bb.array()); } 
		// This should not happen.
		catch (Throwable e) { e.printStackTrace(); }
		return b;
	}
}
