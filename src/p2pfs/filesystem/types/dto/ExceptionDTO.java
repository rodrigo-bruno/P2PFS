package p2pfs.filesystem.types.dto;

/**
 * Class representing an exception.
 * This will be used when some problem happens during a DHT operation. This
 * exception will be delivered to the client side.
 */
public class ExceptionDTO extends FileSystemDTO {

	/**
	 * Constructor. 
	 * @param trowable - the exception
	 */
	public ExceptionDTO(Throwable trowable) 
	{ super(trowable); }
}
