package p2pfs.filesystem;

/**
 * Class representing a file block.
 */
public class Block extends Object {

	/**
	 * Actual block content.
	 */
	private byte[] content;
	
	/**
	 * Constructor.
	 * @param hash
	 * @param size
	 * @param content
	 */
	public Block(long hash, int size, byte[] content) {
		super(hash, size);
		this.type = ObjectType.BLOCK;
	}
	
	/**
	 * Getter.
	 */
	public byte[] getContent() { return this.content; }
	
	

}
