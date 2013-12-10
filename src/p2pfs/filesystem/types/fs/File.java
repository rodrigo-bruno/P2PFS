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
	 * Number of allocated blocks.
	 */
	private int numberBlocks = 0;
	
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
	public int read2(final ByteBuffer buffer, final long size, final long offset, FileSystemBridge fsb) {
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
	
	public int read(final ByteBuffer buffer, final long size, final long offset, FileSystemBridge fsb) {
		if(this.size == 0) { return 0; }
		final int totalCapacity = (int) (this.numberBlocks*this.blockSize);
		final int maxReadIndex = (int) Math.min(size + offset, totalCapacity) - 1;
		final int startBlock = (int) (offset/this.blockSize);
		final int endBlock = (int) (maxReadIndex/this.blockSize);
		final long realReadOffset = offset - startBlock*this.blockSize;
		final int bytesToRead = (int) Math.min(totalCapacity - offset, size);
		final byte[] bytesRead = new byte[bytesToRead];
		System.out.println("size="+size);
		System.out.println("offset="+offset);
		System.out.println("this.size="+this.size);
		System.out.println("maxReadIndex="+maxReadIndex);
		System.out.println("startBlock="+startBlock);
		System.out.println("endBlock="+endBlock);
		System.out.println("bytesToRead="+bytesToRead);
		System.out.println("realReadOffset="+realReadOffset);
		System.out.println("numberBlocks="+this.numberBlocks);
		ByteBuffer bb = getSplittedBlock(startBlock, endBlock, fsb);
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
//		ByteBuffer bb = fsb.getFileBlock(this.name, 0);
//		if (bb != null && size < bb.capacity()) {
//			// Need to create a new, smaller buffer
//			int newCapacity = (int) (size/this.blockSize);
//			newCapacity = size % this.blockSize == 0 ? newCapacity : newCapacity + 1;
//			final ByteBuffer newContents = ByteBuffer.allocate((int) (newCapacity*this.blockSize));
//			final byte[] bytesRead = new byte[(int) size];
//			bb.get(bytesRead);
//			newContents.put(bytesRead);
//			bb = newContents;
//			this.size = bb.capacity(); // Fix file size.
//			fsb.putFileBlock(this.name, 0, bb);
//		}
	}

	/**
	 * Method to write to the file contents.
	 * @param buffer - the bytes to write
	 * @param size - the number of bytes to write
	 * @param offset - the offset for writing.
	 * @return number of bytes written.
	 */
	public int write2(final ByteBuffer buffer, final long bufSize, final long writeOffset, FileSystemBridge fsb){
		final int maxWriteIndex = (int) (writeOffset + bufSize);
		ByteBuffer bb = fsb.getFileBlock(this.name, 0);
		// if the buffer doesn't exist.
		if(bb == null) { bb = ByteBuffer.allocate(0); }
		if (maxWriteIndex > bb.capacity()) {
			// check how many blocks do we need more
			int newCapacity = (int) (maxWriteIndex/this.blockSize);
			newCapacity = maxWriteIndex % this.blockSize == 0 ? newCapacity : newCapacity + 1;
			// Need to create a new, larger buffer
			final ByteBuffer newContents = ByteBuffer.allocate((int) (newCapacity*this.blockSize));
			newContents.put(bb);
			bb = newContents;
		}
		bb.position((int) writeOffset);
		bb.put(buffer);
		bb.position(0); // Rewind
		this.size = bb.capacity(); // Fix file size.
		fsb.putFileBlock(this.name, 0, bb);
		return (int) bufSize;
	}
	
	public int write(final ByteBuffer buffer, final long bufSize, final long writeOffset, FileSystemBridge fsb){
		final int maxWriteIndex = (int) (writeOffset + bufSize) - 1;
		int startBlock = (int) (writeOffset/this.blockSize);
		int endBlock = (int) (maxWriteIndex/this.blockSize);
		ByteBuffer bb = getSplittedBlock(startBlock, endBlock, fsb); // this allocates blocks if needed!	
		long realWriteOffset = writeOffset - startBlock*this.blockSize; 
		
		System.out.println("bufSize="+bufSize);
		System.out.println("writeOffset="+writeOffset);
		System.out.println("maxWriteIndex="+maxWriteIndex);
		System.out.println("startBlock="+startBlock);
		System.out.println("endBlock="+endBlock);
		System.out.println("startBlock="+startBlock);
		System.out.println("realWriteOffset="+realWriteOffset);
		System.out.println("this.numberBlocks="+this.numberBlocks);
		
		bb.position((int) realWriteOffset);
		bb.put(buffer);
		bb.position(0); // Rewind
		putSplittedBlock(bb, startBlock, endBlock, fsb); // this writes all file blocks
		// updating file size
		this.size = this.size < maxWriteIndex + 1 ? maxWriteIndex + 1 : this.size;
		return (int) bufSize;
	}
	
	/**
	 * TODO 
	 * @param startBlock
	 * @param endBlock
	 * @param fsb
	 * @return
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
			System.out.println("GET BLOCK " + index);
			bb.put(fsb.getFileBlock(this.name, index));	
		} 
		bb.position(0); // Rewind
		// updating number of blocks
		this.numberBlocks = this.numberBlocks < endBlock + 1 ? endBlock + 1 : this.numberBlocks;
		return bb;
	}
	
	/**
	 * TODO
	 * @param bb
	 * @param startBlock
	 * @param endBlock
	 * @param fsb
	 * @return
	 */
	boolean putSplittedBlock(final ByteBuffer bb, int startBlock, int endBlock, FileSystemBridge fsb) {
		boolean output = false;
		// for each block, read some bytes and store. 
		// ATTENTION: the size of the buffer must be divisible by the block size. 
		for(	int position = bb.position(),index = startBlock; 
				index <= endBlock; 
				index++,position += this.blockSize) {
			// creating an ephemeral buffer to transfer bytes
			final byte[] transferBuffer = new byte[(int) this.blockSize];
			bb.position(position);
			bb.get(transferBuffer);
			System.out.println("PUT BLOCK " + index);
			boolean tmp = fsb.putFileBlock(this.name, index, ByteBuffer.wrap(transferBuffer));
			output = output ? tmp : false;
		}
		bb.position(0); // Rewind
		return output;
	}
	
}