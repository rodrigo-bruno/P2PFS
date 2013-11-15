/**
 * 
 */
package p2pfs.filesystem.bridges.fuse;

/**
 * CachedBridgeImpl means that the file system API implementation has a caching 
 * mechanism.
 * This class will issue requests to a local cache that will periodically contact
 * the DHT integration layer.
 */
public class CachedBridgeImpl extends FileSystemBridge {

}
