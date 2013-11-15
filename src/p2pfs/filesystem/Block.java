package p2pfs.filesystem;

/**
 * Class representing a file block.
 */
public class Block extends Object {

	/**
	 * Serializable id.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Actual block content.
	 */
	private byte[] content;
	
	/**
	 * Constructor.
	 * @param hash - hash of the content (this might be used to confirm the
	 * content integrity.
	 * @param content - bytes.
	 * @param name - name of the file this block belongs to.
	 */
	public Block(long hash, byte[] content, String name) {
		super(hash, content.length, name);
		this.setType(ObjectType.BLOCK);
	}
	
	/**
	 * Getter.
	 */
	public byte[] getContent() { return this.content; }

	/**
	 * DEBUG only.
	 * FIXME
	 */
	@Override
	public String toString() 
	{ return new String("BLOCK (+"+this.getName()+") -> "+ this.getHash()); }
	
	

}
