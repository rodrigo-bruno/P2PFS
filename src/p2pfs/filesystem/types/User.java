package p2pfs.filesystem.types;

/**
 * Class representing a user.
 */
public class User extends Directory {

	/**
	 * Serializable id.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Number of active mounts;
	 */
	private int numberMounts;
	
	/**
	 * Constructor.
	 */
	public User() { 
		super(null, "~/");
		this.setType(ObjectType.USER);
		this.numberMounts = 0;
	}
	
	/**
	 * DEBUG only.
	 */
	@Override
	public String toString() {
		String out = new String("Active Mounts=" + this.numberMounts + "\n");
		return out + super.toString();
	}
	
	/**
	 * Getter.
	 */
	public int getNumberMounts() { return this.numberMounts; }

}
