package p2pfs.filesystem.types.fs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.fusejna.DirectoryFiller;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.NodeType;

/**
 * Class representing a directory object.
 */
public class Directory extends Path implements Serializable {
	
	/**
	 * Serialization id. 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * List of containing file system objects.
	 */
	protected final List<Path> contents = new ArrayList<Path>();

	/**
	 * Constructor.
	 * @param name - see base doc.
	 */
	public Directory(final String name)
	{ super(name); }

	/**
	 * Constructor.
	 * @param name - see base doc.
	 * @param parent - see base doc.
	 */
	public Directory(final String name, final Directory parent)
	{ super(name, parent); }

	/**
	 * Method to add a new object to this directory.
	 * @param p - the new object,
	 */
	public void add(final Path p) {
		contents.add(p);
		p.parent = this;
	}

	/**
	 * Method to find a file system object.
	 * @param path - path to the target object.
	 * @return - the object or null.
	 */
	@Override
	public Path find(String path) 	{
		// Test if this is the target object.
		if (super.find(path) != null) 
		{ return super.find(path); }
		// Removes all the initial slashes.
		while (path.startsWith("/")) 
		{ path = path.substring(1); }
		// Checks if it is still a composed path.
		if (!path.contains("/")) {
			for (final Path p : contents) 
			{ if (p.name.equals(path)) { return p;	} }
			return null;
		}

		final String nextName = path.substring(0, path.indexOf("/"));
		final String rest = path.substring(path.indexOf("/"));
		// Recursive searching.
		for (final Path p : contents) 
		{ if (p.name.equals(nextName)) { return p.find(rest); } }
		// Not found.
		return null;
	}

	/**
	 * see base doc.
	 */
	@Override
	public void getattr(final StatWrapper stat)
	{ stat.setMode(NodeType.DIRECTORY); }

	/**
	 * Method to create a new directory.
	 * @param lastComponent - the name of the new directory.
	 */
	public void mkdir(final String lastComponent)
	{ contents.add(new Directory(lastComponent, this)); }

	/**
	 * Method to create a new file.
	 * @param lastComponent - the name of the new file.
	 */
	public void mkfile(final String lastComponent)
	{ contents.add(new File(lastComponent, this)); }

	/**
	 * Method that reads the contents of the directory.
	 * @param filler - where to place the items.
	 */
	public void read(final DirectoryFiller filler)
	{ for (final Path p : contents)	{ filler.add(p.name); }	}
}