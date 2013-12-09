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
	 * to download again a file block or not. -> feito na camada acima?
	 * 
	 * usar metodo aux: 
	 * ByteBuffer getMergedBlock(final long size, final long offset)
	 * boolean putMergedBlock(ByteBuffer bb, int offset)
	 * 
	 * read:
	 * ver o offset para ver o bloco inicial.
	 * ver size: pode ser preciso usar mais mais blocos
	 * start_block = offset%block_size 
	 * 
	 */
	
	/**
	 * Size of the file (in bytes).
	 */
	private long size = 0;
	
	/**
	 * Max size for each block (in bytes) = 128Kb.
	 */
	private long blockSize = 128*1024;
	
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
	public void getattr(final StatWrapper stat) {
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
		ByteBuffer bb = fsb.getFileBlock(this.name, 0);
		if (bb != null && size < bb.capacity()) {
			// Need to create a new, smaller buffer
			final ByteBuffer newContents = ByteBuffer.allocate((int) size);
			final byte[] bytesRead = new byte[(int) size];
			bb.get(bytesRead);
			newContents.put(bytesRead);
			bb = newContents;
			this.size = bb.capacity(); // Fix file size.
			fsb.putFileBlock(this.name, 0, bb);
		}
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
			// check how many blocks do we need more
			int newCapacity = (int) (maxWriteIndex/this.blockSize);
			newCapacity = maxWriteIndex % this.blockSize == 0 ? newCapacity : newCapacity + 1;
			// Need to create a new, larger buffer
			final ByteBuffer newContents = ByteBuffer.allocate((int) (newCapacity*this.blockSize)); // TODO: debug
			newContents.put(bb);
			bb = newContents;
		}
		final byte[] bytesToWrite = new byte[(int) bufSize];
		buffer.get(bytesToWrite, 0, (int) bufSize);
		bb.position((int) writeOffset);
		bb.put(bytesToWrite);
		bb.position(0); // Rewind
		this.size = bb.capacity(); // Fix file size.
		fsb.putFileBlock(this.name, 0, bb);
		return (int) bufSize;
	}
	
	/**
	 * Auxiliary method to obtain multiple file blocks and merge them into a 
	 * single byte buffer.
	 * The size and offset are used to determine which file blocks will be used.
	 * These file blocks are retrieved and merged.
	 * Further operations should use offset %= this.blockSize. 
	 * @param size
	 * @param offset
	 * @param fsb
	 * @return
	 */
	ByteBuffer getSplittedBlock(final long size, final long offset, FileSystemBridge fsb) {
		// calculate the first and last needed blocks.
		int startBlock = (int) (offset / this.blockSize);
		int endBlock = (int) ((offset + size) / this.blockSize);
		// retrieve all the needed file parts
		ByteBuffer[] bbarray = new ByteBuffer[endBlock-startBlock + 1];
		for(int index = 0 ; index < bbarray.length; index++) 
		{ bbarray[index] = fsb.getFileBlock(this.name, index + startBlock);	}
		// create the array that will contain all the file parts
		int capacity = (int) (this.blockSize*(bbarray.length - 1) + bbarray[bbarray.length - 1].capacity());
		ByteBuffer bb = ByteBuffer.allocate(capacity);
		for(int index = 0; index < bbarray.length; index++) 
		{ bb.put(bbarray[index].array()); }
		bb.rewind();
		return bb;
	}
	
	/**
	 * TODO!
	 * @param bb
	 * @param size
	 * @param offset
	 * @param fsb
	 * @return
	 */
	boolean putSplittedBlock(ByteBuffer bb, final long size, final long offset, FileSystemBridge fsb) {
		return false;
	}
	
}