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

	public SimpleBridgeImpl(KademliaBridge dht) {
		super(dht);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Directory getHomeDirectory(String username) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ByteBuffer getFileBlock(String filePath, int blockNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putHomeDirectory(String username, Directory directory) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void putFileBlock(String filePath, int blockNumber, ByteBuffer buffer) {
		// TODO Auto-generated method stub
		
	}

}
