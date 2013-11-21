package p2pfs.filesystem.types.dto;

/**
 * DTO representing the state and possible response of a DHT operation.
 */
public class OperationCompleteDTO extends FileSystemDTO {

	/**
	 * Serialization id. 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Status of the operation. Can be successful or failed.
	 */
	private boolean status;
	
	/**
	 * Constructor.
	 * @param object - the object to be sent. It may be null if status == failed.
	 * @param status
	 */
	public OperationCompleteDTO(Object object, boolean status) {
		super(object);
		this.status = status;
	}
	
	/**
	 * Getter.
	 * @return
	 */
	public boolean getStatus() { return this.status; }

}
