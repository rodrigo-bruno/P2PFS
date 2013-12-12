package p2pfs.filesystem.layers.cache;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.tomp2p.peers.Number160;

import p2pfs.filesystem.layers.bridge.KademliaBridge;
import p2pfs.filesystem.types.fs.Directory;

/**
 * CachedBridgeImpl means that the file system API implementation has a caching 
 * mechanism.
 * This class will issue requests to a local cache that will periodically contact
 * the DHT integration layer.
 * Write-back!
 */
public class CachedBridgeImpl extends SimpleBridgeImpl {
		
	/**
	 * Class representing the objects that will be cached. This class is used so
	 * that we have additional information for managing the object life time in
	 * cache.
	 */
	class CachedObject {
				
		/**
		 * Real cached object.
		 */
		Object o = null;
		
		/**
		 * Time until the object is flushed (this time may be increased).
		 */
		int timeToFlush = MIN_TIME_IN_CACHE;
		
		/**
		 * Total amount of time that the object is in cache.
		 */
		int timeInCache = 0;
		
		/**
		 * Represents if the object was read or not.
		 * This will produce different behavior when flushing.
		 */
		boolean read = false;
		
		/**
		 * Hash code for the object o.
		 */
		int hash = 0;
		
		/**
		 * Constructor.
		 * @param o - see doc.
		 */
		CachedObject(Object o, boolean read, int hash) { 
			this.o = o;
			this.read = read;
			this.hash = hash;
		}
		
		CachedObject(Object o, boolean read) {
			this.read = read;
			this.o = o;
		}
		
		/**
		 * Getters.
		 */
		Object getObject() { return this.o; }
		int getTimeToFlush() { return this.timeToFlush; }
		int getTimeInCache() { return this.timeInCache; }
		boolean getRead() { return this.read; }
		int getHash() { return this.hash; }
		
		/**
		 * Setters.
		 */
		void setTimeToFlush(int timeToFlush) { this.timeToFlush = timeToFlush; }
		void setTimeInCache(int timeInCache) { this.timeInCache = timeInCache; }
		void setObject(Object o) { this.o = o; }
		void setRead(boolean read) { this.read = read; }
		
	}
	
	/**
	 * Class representing the runnable that will handle the cache refreshing.
	 */
	class CacheRunnable implements Runnable {
		
		/**
		 * Reference to the holder object.
		 */
		CachedBridgeImpl cdi = null;
		
		/**
		 * Constructor.
		 */
		public CacheRunnable(CachedBridgeImpl cdi) { this.cdi = cdi; }
		
		/**
		 * Method that does the job.
		 * This method refreshed the cache and flushed objects when needed.
		 */
		@Override
		public void run() {
			
			// just to make lines smaller.
			int ri = CachedBridgeImpl.REFRESH_INTERVAL;
			int Mtic = CachedBridgeImpl.MAX_TIME_IN_CACHE;
			while(true) {
				System.out.println("Cache refresh begun!");
				int sentBlocks = 0;
				synchronized (this.cdi) {
					// list that will keep flushed objects.
					List<Number160> removed = new ArrayList<Number160>();; 
					// iterate over all the objects.
					for(Map.Entry<Number160, CachedObject> entry : CachedBridgeImpl.cache.entrySet()) {
						CachedObject co = entry.getValue();
						int tic = co.getTimeInCache() + ri;
						int ttf = co.getTimeToFlush() - ri; 
						
						System.out.println("Cached Object: tic="+tic+", ttf="+ttf+", dirty="+!co.getRead());
						
						co.setTimeInCache(tic);
						co.setTimeToFlush(ttf);
						// if the object should be flushed.
						if(tic >= Mtic || ttf <= 0) {
							// if the file was modified. Reads are just discarded.
							if (!co.getRead()) { 
								Object o = co.getObject();
								if(o instanceof Directory) { 
									this.cdi.superPutHomeDirectory(entry.getKey(), (Directory)o);
									// force re-fetch to keep meta data up to date.
									removed.add(entry.getKey()); 
								} 
								else if (o instanceof ByteBuffer) { 
									if(sentBlocks++ < CachedBridgeImpl.MAX_FLUSH_BURST) {
										this.cdi.superPutFileBlock(entry.getKey(), (ByteBuffer)o, co.getHash());
										// after flushing, keep a copy
										co.setRead(true);
									}
								} 
								else { 
									try { throw new Exception("Unknown cache object class!"); }
									catch (Exception e) { e.printStackTrace(); }
								}
							}
							else {
								removed.add(entry.getKey()); // perform hash comparizon
							}
						}
					}
					// remove objects from cache. // this will no longer be neede?
					for(Number160 key : removed) { CachedBridgeImpl.cache.remove(key); }
				}
				System.out.println("Cache refresh ended!");
				try { Thread.sleep(CachedBridgeImpl.REFRESH_INTERVAL*1000); } 
				// if the thread is interrupted while sleeping.
				catch (InterruptedException e) { e.printStackTrace(); }
			}
		}		
	}
	
	/**
	 * The minimum number of seconds that an object may stay in cache.
	 * When an object is written: timeToFlush = MIN_TIME_IN_CACHE;
	 */
	public static int MIN_TIME_IN_CACHE = 30;
	
	/**
	 * The maximum number of seconds that an object may stay in cache.
	 * When an object is written: timeInCache += (MIN_TIME_IN_CACHE - timeToFlush)  
	 */
	public static int MAX_TIME_IN_CACHE = 120;
	
	/**
	 * Number of seconds between a cache check.
	 */
	public static int REFRESH_INTERVAL = 1;
	
	/**
	 * TODO
	 */
	public static int MAX_FLUSH_BURST = 10;
	
	/**
	 * The cache itself =)
	 */
	private static Map<Number160, CachedObject> cache = new HashMap<Number160, CachedObject>();
	
	/**
	 * Constructor.
	 * Creates and starts the cache thread.
	 * @param dht - see base doc.
	 */
	public CachedBridgeImpl(KademliaBridge dht) { 
		super(dht);
		Thread ct = new Thread(new CacheRunnable(this));
		ct.setDaemon(true);
		ct.start();
	}

	/**
	 * In addition to the base doc, this method tries to use the cache. If
	 * its not possible, it uses the DHT, stores the result on cache and returns.
	 */
	@Override
	public Directory getHomeDirectory(Number160 key) {
		synchronized (this) {
			if(CachedBridgeImpl.cache.containsKey(key)) {
				CachedObject co = CachedBridgeImpl.cache.get(key);
				co.setTimeInCache(co.getTimeInCache() + MIN_TIME_IN_CACHE - co.getTimeToFlush());
				co.setTimeToFlush(MIN_TIME_IN_CACHE);
			} 
			else 
			{ CachedBridgeImpl.cache.put(key, new CachedObject(super.getHomeDirectory(key), true)); }
			return (Directory)CachedBridgeImpl.cache.get(key).getObject();
		}
	}

	/**
	 * In addition to the base doc, this method tries to use the cache. If
	 * its not possible, it uses the DHT, stores the result on cache and returns.
	 */
	@Override
	public ByteBuffer getFileBlock(Number160 key, int hash) {
		synchronized (this) {
			if(CachedBridgeImpl.cache.containsKey(key)) {
				CachedObject co = CachedBridgeImpl.cache.get(key);
				co.setTimeInCache(co.getTimeInCache() + MIN_TIME_IN_CACHE - co.getTimeToFlush());
				co.setTimeToFlush(MIN_TIME_IN_CACHE);
				// test if the hash matched
				// if yes, okey
				// if not, refetch
			} 
			else 
			{ CachedBridgeImpl.cache.put(key, new CachedObject(super.getFileBlock(key,hash), true, hash)); }
			// To maintain the coherence with the other implementation (and to 
			// prevent strange bugs) we return a copy of the array. 
			ByteBuffer cached = (ByteBuffer)CachedBridgeImpl.cache.get(key).getObject();
			cached.position(0);
			byte[] tmp = new byte[cached.capacity()];
			cached.get(tmp);
			return ByteBuffer.wrap(tmp);
		}
	}

	/**
	 * In addition to the base doc, this method tries to use the cache. If
	 * its not possible, it uses the DHT, stores the result on cache and returns.
	 */
	@Override
	public boolean putHomeDirectory(Number160 key, Directory directory) {
		synchronized (this) {
			if(CachedBridgeImpl.cache.containsKey(key)) { 
				CachedObject co = CachedBridgeImpl.cache.get(key);
				co.setRead(false);
				co.setObject(directory);
				co.setTimeInCache(co.getTimeInCache() + MIN_TIME_IN_CACHE - co.getTimeToFlush());
				co.setTimeToFlush(MIN_TIME_IN_CACHE);
			}
			else { CachedBridgeImpl.cache.put(key, new CachedObject(directory, false)); }
			return true; // TODO: Fake true... Better solution?
		}
		
	}

	/**
	 * In addition to the base doc, this method tries to use the cache. If
	 * its not possible, it uses the DHT, stores the result on cache and returns.
	 */
	@Override
	public boolean putFileBlock(Number160 key, ByteBuffer buffer, int hash) {
		synchronized (this) {
			if(CachedBridgeImpl.cache.containsKey(key)) { 
				CachedObject co = CachedBridgeImpl.cache.get(key);
				co.setRead(false);
				co.setObject(buffer);
				co.setTimeInCache(co.getTimeInCache() + MIN_TIME_IN_CACHE - co.getTimeToFlush());
				co.setTimeToFlush(MIN_TIME_IN_CACHE);
			}
			else { CachedBridgeImpl.cache.put(key, new CachedObject(buffer, false, hash)); }
			return true; // TODO: Fake true... Better solution?
		}
	}
	
	/**
	 * Methods to force the super class implementation (without cache)
	 * These methods are not synchronized!
	 */
	public boolean superPutHomeDirectory(Number160 key, Directory directory) 
	{ return super.putHomeDirectory(key, directory); }
	public boolean superPutFileBlock(Number160 key, ByteBuffer buffer, int hash) 
	{ return super.putFileBlock(key, buffer, hash); }
	
}
