package p2pfs.filesystem;

/**
 * Class representing a user.
 */
public class User extends Directory {

	/**
	 * Number of active mounts;
	 */
	private int numberMounts;
	
	/**
	 * Constructor.
	 */
	public User() { 
		super("~/");
		this.type = ObjectType.USER;
	}
	
	/**
	 * Getter.
	 */
	public int getNumberMounts() { return this.numberMounts; }

}
