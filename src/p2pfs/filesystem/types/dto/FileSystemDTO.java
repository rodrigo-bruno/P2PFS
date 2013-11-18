package p2pfs.filesystem.types.dto;

/**
 * This class is the base class for all Data Transfers Objects that will be 
 * sent via socket to the PeerThread thread.
 */
public abstract class FileSystemDTO {
	
	/**
	 * Object that will be sent through the net.
	 */
	protected final Object object;
	
	/**
	 * Constructor.
	 * @param object
	 */
	public FileSystemDTO(Object object)
	{ this.object = object; }

}
