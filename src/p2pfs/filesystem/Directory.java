package p2pfs.filesystem;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a directory.
 */
public class Directory extends File {

	/**
	 * List of files within this directory.
	 */
	private List<File> files;

	
	/**
	 * Constructor.
	 * @param path
	 */
	public Directory(String path) {
		super(0, 0, path);
		this.files = new ArrayList<File>();
	}
	
	/**
	 * Constructor.
	 * @param hash - hash of the object.
	 * @param numberParts
	 * @param size - size of the list in bytes.
	 * @param path
	 * @param files - list of files within the directory.
	 */
	public Directory(
			long hash, 
			int size, 
			String path, 
			List<File> files) {
		super(hash, size, path);
		this.type = ObjectType.DIRECTORY;
		this.files = files;
	}
	
	/**
	 * Getter.
	 */
	public List<File> getFiles() { return this.files; }

}
