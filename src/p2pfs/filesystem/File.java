package p2pfs.filesystem;

/**
 * Class representing a file.
 */
public class File extends Object {
	
	/**
	 * FS path of the file.
	 */
	private String path;
	
	/**
	 * Constructor
	 * @param hash - hash of the all file.
	 * @param size
	 */
	public File(long hash, int size, String path) {
		super(hash, size);
		this.type = ObjectType.FILE;
		this.path = path;
	}
	
	/**
	 * Getters
	 */
	public String getPath() { return this.path; }

}
