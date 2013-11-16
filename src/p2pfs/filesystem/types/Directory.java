package p2pfs.filesystem.types;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class representing a directory.
 */
public class Directory extends File {

	/**
	 * Serializable id.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * List of files within this directory.
	 */
	private List<File> files;

	
	/**
	 * Constructor.
	 * @param name
	 */
	public Directory(Directory parent, String name) {
		super(parent, 0, 0, name);
		this.files = new ArrayList<File>();
	}
	
	/**
	 * Constructor.
	 * @param hash - hash of the object.
	 * @param numberParts
	 * @param size - size of the list in bytes.
	 * @param name
	 * @param files - list of files within the directory.
	 */
	public Directory(
			Directory parent,
			long hash, 
			int size, 
			String name, 
			List<File> files) {
		super(parent, hash, size, name);
		this.setType(ObjectType.DIRECTORY);
		this.files = files;
	}
	
	/**
	 * DEBUG only.
	 */
	@Override
	public String toString() {
		String out = new String(this.getObjectType() +"\t"+this.getSize() + "\t.\n");
		for(Iterator<File> it = files.iterator(); it.hasNext();) {
			out += it.next().toString();
		}
		return out;
	}
	
	/**
	 * Getter.
	 */
	public List<File> getFiles() { return this.files; }

}