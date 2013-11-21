package p2pfs.filesystem.types.dto;

import java.io.Serializable;

/**
 * This class is the base class for all Data Transfers Objects that will be 
 * sent via socket to the PeerThread thread.
 */
public abstract class FileSystemDTO implements Serializable{
	
	/**
	 * Serialization id. 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Object that will be sent through the net.
	 */
	protected final Object object;
	
	/**
	 * Constructor.
	 * @param object
	 */
	public FileSystemDTO(Object object)	{ this.object = object; }
	
	/**
	 * Getter.
	 * @return - the object.
	 */
	public Object getObject() { return this.object; }

}
