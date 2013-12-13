package p2pfs.filesystem.types.fs;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

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
	 * Mapping between a file block and a hash. This will be used to maitain in
	 * cache up to date copies of blocks.
	 */
	private final Map<String, Integer> hashes = new HashMap<String, Integer>();
	
	/**
	 * Size of the file (in bytes).
	 */
	private long size = 0;
	
	/**
	 * Number of allocated blocks.
	 */
	private int numberBlocks = 0;
	
	/**
	 * Max size for each block (in bytes) = 32Kb.
	 */
	private long blockSize = 32*1024;
	
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
		// check if it is an empty file.
		if(this.size == 0) { return 0; }
		// compute some information regarding the requested read.
		final int totalCapacity = (int) (this.numberBlocks*this.blockSize);
		final int maxReadIndex = (int) Math.min(size + offset, totalCapacity) - 1;
		final int startBlock = (int) (offset/this.blockSize);
		final int endBlock = (int) (maxReadIndex/this.blockSize);
		final long realReadOffset = offset - startBlock*this.blockSize;
		final int bytesToRead = (int) Math.min(totalCapacity - offset, size);
		// intermediate array
		final byte[] bytesRead = new byte[bytesToRead]; // TODO: check if is necessary.
		// do the actual reading
		ByteBuffer bb = getSplittedBlock(startBlock, endBlock, fsb);
		// copy to the intermediate array, and finally to the output array.
		bb.position((int) realReadOffset);
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
		this.size = size;
		this.numberBlocks = (int) (size/this.blockSize);
	}

	/**
	 * Method to write to the file contents.
	 * @param buffer - the bytes to write
	 * @param size - the number of bytes to write
	 * @param offset - the offset for writing.
	 * @return number of bytes written.
	 */
	public int write(final ByteBuffer buffer, final long bufSize, final long writeOffset, FileSystemBridge fsb){
		// compute some information regarding the requested read.
		final int maxWriteIndex = (int) (writeOffset + bufSize) - 1;
		int startBlock = (int) (writeOffset/this.blockSize);
		int endBlock = (int) (maxWriteIndex/this.blockSize);
		long realWriteOffset = writeOffset - startBlock*this.blockSize;
		// retrieve the file segment where the write will be performed.
		ByteBuffer bb = getSplittedBlock(startBlock, endBlock, fsb);	
		// prepare the write and write.
		bb.position((int) realWriteOffset);
		bb.put(buffer);
		bb.position(0);
		// commit write.
		putSplittedBlock(bb, startBlock, endBlock, fsb);
		// updating file size
		this.size = this.size < maxWriteIndex + 1 ? maxWriteIndex + 1 : this.size;
		return (int) bufSize;
	}
	
	/**
	 * Auxiliary method to retrieve a file segment.
	 * The startBlock and endBlock should be used carefully. Take into attention
	 * that this method will grow the file size if needed.
	 * This method returns a fresh ByteBuffer ready to be used (no need to copy). 
	 * @param startBlock - the first block index
	 * @param endBlock - the last block index
	 * @param fsb - the file system bridge (where to get blocks)
	 * @return - a byte buffer with the requested blocks.
	 */
	ByteBuffer getSplittedBlock(int startBlock, int endBlock, FileSystemBridge fsb) {
		int nblocks = endBlock - startBlock + 1;
		// create the array that will contain all the file parts
		ByteBuffer bb = ByteBuffer.allocate((int) (this.blockSize*nblocks));
		// fill array with existent chunks
		for(	int position = bb.position(), index = startBlock; 
				index <= endBlock && index < this.numberBlocks; 
				index++, position += this.blockSize) {  
			bb.position(position);
			int hash; 
			synchronized(this.hashes) { hash = this.hashes.get(new String(this.name+index)).intValue(); }
			System.out.println("GET BLOCK " + index + ", hash="+hash);
			bb.put(fsb.getFileBlock(this.name, index, hash));	
		} 
		bb.position(0);
		// updating number of blocks
		this.numberBlocks = this.numberBlocks < endBlock + 1 ? endBlock + 1 : this.numberBlocks;
		return bb;
	}
	
	/**
	 * Auxiliary method to write a file segment.
	 * The startBlock and endBlock should be used carefully. Take into attention
	 * that this method assumes that the buffer given as input will have all
	 * the file parts from startBlock to endBlock ([start, end]).
	 * @param bb - the buffer to be written
	 * @param startBlock - the first block index
	 * @param endBlock - the last block index
	 * @param fsb - the file system bridge (where to get blocks)
	 * @return - a boolean representing success or not.
	 */
	boolean putSplittedBlock(final ByteBuffer bb, int startBlock, int endBlock, FileSystemBridge fsb) {
		boolean output = false;
		// for each block, copy bytes and store. 
		for(	int position = bb.position(),index = startBlock; 
				index <= endBlock; 
				index++,position += this.blockSize) {
			// creating an ephemeral buffer to transfer bytes
			final byte[] transferBuffer = new byte[(int) this.blockSize];
			bb.position(position);
			bb.get(transferBuffer);
			// put hash code into map
			int hash = transferBuffer.hashCode();
			synchronized(this.hashes) { this.hashes.put(new String(this.name+index), hash); }
			System.out.println("PUT BLOCK " + index + ", hash="+hash);
			boolean tmp = fsb.putFileBlock(this.name, index, ByteBuffer.wrap(transferBuffer),hash );
			output = output ? tmp : false;
		}
		bb.position(0);
		return output;
	}
	
}