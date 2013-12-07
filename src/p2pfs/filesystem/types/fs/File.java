package p2pfs.filesystem.types.fs;

import java.io.Serializable;
import java.nio.ByteBuffer;

import p2pfs.filesystem.layers.cache.FileSystemBridge;

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
	public int read(final ByteBuffer buffer, final long size, final long offset, FileSystemBridge fsb) {
		ByteBuffer bb = fsb.getFileBlock(this.name, 0);
		if (bb == null) { return 0; }
		final int bytesToRead = (int) Math.min(bb.capacity() - offset, size);
		final byte[] bytesRead = new byte[bytesToRead];
		bb.position((int) offset);
		bb.get(bytesRead, 0, bytesToRead);
		buffer.put(bytesRead);
		bb.position(0); // Rewind
		return bytesToRead;
	}

	/**
	 * Method to truncate the file's contents.
	 * @param size - the final file size.
	 */
	public void truncate(final long size, FileSystemBridge fsb){
//		ByteBuffer bb = fsb.getFileBlock(this.name, 0);
//		if (bb != null && size < bb.capacity()) {
//			// Need to create a new, smaller buffer
//			final ByteBuffer newContents = ByteBuffer.allocate((int) size);
//			final byte[] bytesRead = new byte[(int) size];
//			bb.get(bytesRead);
//			newContents.put(bytesRead);
//			bb = newContents;
//			fsb.putFileBlock(this.name, 0, bb);
//		}
	}

	/**
	 * Method to write to the file contents.
	 * @param buffer - the bytes to write
	 * @param size - the number of bytes to write
	 * @param offset - the offset for writing.
	 * @return success or not.
	 */
	public int write(final ByteBuffer buffer, final long bufSize, final long writeOffset, FileSystemBridge fsb){
		final int maxWriteIndex = (int) (writeOffset + bufSize);
		ByteBuffer bb = fsb.getFileBlock(this.name, 0);
		// if the buffer doesn't exist.
		if(bb == null) { bb = ByteBuffer.allocate(0); }
		if (maxWriteIndex > bb.capacity()) {
			// Need to create a new, larger buffer
			final ByteBuffer newContents = ByteBuffer.allocate(maxWriteIndex);
			newContents.put(bb);
			bb = newContents;
		}
		final byte[] bytesToWrite = new byte[(int) bufSize];
		buffer.get(bytesToWrite, 0, (int) bufSize);
		bb.position((int) writeOffset);
		bb.put(bytesToWrite);
		bb.position(0); // Rewind
		this.size = bb.capacity(); // FIXME: right? It seems to be!
		fsb.putFileBlock(this.name, 0, bb);
		return (int) bufSize;
	}
}