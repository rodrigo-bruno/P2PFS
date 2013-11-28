package p2pfs.filesystem.types.fs;

import java.io.Serializable;
import java.nio.ByteBuffer;

import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.NodeType;

/**
 * Class representing a file object.
 */
public class File extends Path implements Serializable {

	/**
	 * Serialization id. 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * TODO: we need the hashes of the file blocks so that we can see if we need
	 * to download again a file block or not.
	 */
	
	/**
	 * Size of the file (in bytes).
	 */
	private long size = 0;
	
	/**
	 * Constructor.
	 * @param name - see base doc.
	 */
	public File(final String name)
	{ super(name); }

	/**
	 * Constructor.
	 * @param name - see base doc.
	 * @param parent - see base doc.
	 */
	public File(final String name, final Directory parent)
	{ super(name, parent); }

	/**
	 * see base doc.
	 */
	@Override
	public void getattr(final StatWrapper stat)
	{
		stat.setMode(NodeType.FILE);
		stat.size(this.size);
	}

	/**
	 * Method to read the file contents.
	 * @param buffer - where to place the bytes.
	 * @param size - the number of bytes to read
	 * @param offset - the offset
	 * @return success or not.
	 */
	public int read(final ByteBuffer buffer, final long size, final long offset) {
		// TODO: implement me.
		return 0; 
	}

	/**
	 * Method to truncate the file's contents.
	 * @param size - the final file size.
	 */
	public void truncate(final long size){
		// TODO: implement me. 
	}

	/**
	 * Method to write to the file contents.
	 * @param buffer - the bytes to write
	 * @param size - the number of bytes to write
	 * @param offset - the offset for writing.
	 * @return success or not.
	 */
	public int write(final ByteBuffer buffer, final long bufSize, final long writeOffset){
		// TODO: implement me.
		return 0; 
	}
}