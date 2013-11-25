package p2pfs.filesystem.layers.cache;

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

}
