package p2pfs.filesystem;

import java.io.Serializable;

/**
 * Abstract class representing a generic file system object.
 */
public abstract class Object implements Serializable {
	
	/**
	 * Serializable id.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Object type.
	 */
	private ObjectType type;
	
	/**
	 * Objects have an hash of its content.
	 */
	private long hash = 0;
	
	/**
	 * Size of the all file in bytes.
	 */
	private int size;
	
	/**
	 * Object's name.
	 */
	private String name;
	
	/**
	 * Constructor.
	 * @param type - the type of the object.
	 */
	public Object(long hash, int size, String name) { 
		this.hash = hash;
		this.size = size;
		this.name = name;
	}

	protected void setType(ObjectType type) { this.type = type; }
	
	/**
	 * Getters
	 */
	public ObjectType getObjectType() { return this.type; }
	public long getHash() { return this.hash; }
	public int getSize() { return this.size; }
	public String getName() { return this.name; }
	
	/**
	 * DEBUG only.
	 */
	public abstract String toString();
}
