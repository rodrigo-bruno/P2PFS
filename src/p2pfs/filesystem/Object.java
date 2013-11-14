package p2pfs.filesystem;

/**
 * Abstract class representing a generic file system object.
 */
public abstract class Object {
	
	/**
	 * Object type.
	 */
	protected ObjectType type;
	
	/**
	 * Objects have an hash of its content.
	 */
	private long hash = 0;
	
	/**
	 * Size of the all file in bytes.
	 */
	private int size;
	
	/**
	 * Constructor.
	 * @param type - the type of the object.
	 */
	public Object(long hash, int size) { 
		this.hash = hash;
		this.size = size;
	}

	/**
	 * Getters
	 */
	public ObjectType getObjectType() { return this.type; }
	public long getHash() { return this.hash; }
	public int getSize() { return this.size; }
}
