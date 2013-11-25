package p2pfs.filesystem.types;

/**
 * Class representing a file metadata.
 */
public class File extends Object {
	
	/**
	 * Serializable id.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Parent directory.
	 */
	private Directory parent;
	
	/**
	 * Constructor.
	 * @param hash - hash of the all file.
	 * @param size - sum of the number of bytes for all blocks.
	 */
	public File(Directory parent, long hash, int size, String name) {
		super(hash, size, name);
		this.setType(ObjectType.FILE);
		this.parent = parent;
	}
	
	/**
	 * DEBUG only.
	 */
	@Override
	public String toString() 
	{ return new String(this.getSize() + "\t"+this.getName()+"\n"); }
	
	/**
	 * Getter.
	 */
	public Directory getParent() { return this.parent; }
}
