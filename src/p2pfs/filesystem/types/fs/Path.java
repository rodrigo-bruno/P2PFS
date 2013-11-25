package p2pfs.filesystem.types.fs;

import net.fusejna.StructStat.StatWrapper;

/**
 * Class representing a file system object.
 */
public abstract class Path {
	
	/**
	 * Name of the object.
	 */
	protected String name;
	
	/**
	 * Object's parent.
	 */
	protected Directory parent;

	/**
	 * Constructor.
	 * @param name - name of the object.
	 */
	public Path(final String name) { this(name, null); }

	/**
	 * Constructor.
	 * @param name - name of the object.
	 * @param parent - object's parent.
	 */
	public Path(final String name, final Directory parent) {
		this.name = name;
		this.parent = parent;
	}

	/**
	 * Method to delete the object.
	 */
	public void delete()
	{
		if (parent != null) {
			parent.contents.remove(this);
			parent = null;
		}
	}

	/**
	 * Method to find a file system object.
	 * This method starts by removing all the initial slashes.
	 * Then it checks if the target object is this one.
	 * @param path - path to the target object.
	 * @return - the object or null.
	 */
	public Path find(String path) {
		while (path.startsWith("/")) { path = path.substring(1); }
		if (path.equals(name) || path.isEmpty()) { return this; }
		return null;
	}

	/**
	 * Method to get the object type.
	 * @param stat - where to put the answer.
	 */
	public abstract void getattr(StatWrapper stat);

	/**
	 * Rename method.
	 * @param newName
	 */
	public void rename(String newName)	{
		while (newName.startsWith("/"))	{ newName = newName.substring(1); }
		name = newName;
	}
}
