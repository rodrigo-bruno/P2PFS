package p2pfs.filesystem.layers.cache;

import java.nio.ByteBuffer;

import p2pfs.filesystem.layers.bridge.KademliaBridge;
import p2pfs.filesystem.types.fs.Directory;

/**
 * CachedBridgeImpl means that the file system API implementation has a caching 
 * mechanism.
 * This class will issue requests to a local cache that will periodically contact
 * the DHT integration layer.
 */
public class CachedBridgeImpl extends FileSystemBridge {

	// TODO: posso guardar um map (key, object). Assim apenas guardo o objeto mais recente.
	// TODO: map tambem tem de servir para leitura
	
	// TODO: threads para manter o homedir atualizado (leitura e escrita)
	
	public CachedBridgeImpl(KademliaBridge dht) {
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
