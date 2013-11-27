package p2pfs.filesystem.layers.fuse;

import java.nio.ByteBuffer;

import p2pfs.filesystem.layers.cache.FileSystemBridge;
import p2pfs.filesystem.types.fs.Directory;
import p2pfs.filesystem.types.fs.Path;
import p2pfs.filesystem.types.fs.File;

import net.fusejna.DirectoryFiller;
import net.fusejna.ErrorCodes;
import net.fusejna.FuseException;
import net.fusejna.StructFuseFileInfo.FileInfoWrapper;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.ModeWrapper;
import net.fusejna.util.FuseFilesystemAdapterAssumeImplemented;

/**
 * Class that implements the FUSE interface.
 * This code was adapted from package net.fusejna.examples.MemoryFS.
 */
public class Fuse extends FuseFilesystemAdapterAssumeImplemented {
	
	/**
	 * The bridge to access the P2P FS. It will be used to retrieve and store
	 * objects to the remote file system.
	 */
	protected final FileSystemBridge fsb;
	
	/**
	 * The username, used to access the user's metadata.
	 */
	protected final String username;

	/**
	 * Constructor. Mounts the file system.
	 * @param username
	 * @param mountpoint
	 * @throws FuseException - if FUSE fails. 
	 */
	public Fuse(FileSystemBridge fsb, String username, String mountpoint) throws FuseException {
		// TODO: sempre que houver uma alteracao, guardar o homedir
		// TODO: tentar passar o homedir para string? - proximo passo - visitor
		this.fsb = fsb;
		this.username = username;
		this.mount(mountpoint);
	}
	
	/**
	 * Auxiliary method to get the user home directory (the file system root).
	 * @return - the root directory.
	 */
	protected Directory getHomeDirectory() 
	{ return this.fsb.getHomeDirectory(this.username); }

	/**
	 * Auxiliary method to get the parent of a given path.
	 * @return - a path (might be a file or a folder).
	 */
	private Path getParentPath(final String path)
	{ return this.getHomeDirectory().find(path.substring(0, path.lastIndexOf("/"))); }
	
	/**
	 * Auxiliary method to get the last element of a given path.
	 * @return - the folder/directory name.
	 */
	private String getLastComponent(String path) {
		while (path.substring(path.length() - 1).equals("/")) 
		{ path = path.substring(0, path.length() - 1); }
		if (path.isEmpty()) 
		{ return ""; }
		return path.substring(path.lastIndexOf("/") + 1);
	}
	
	@Override
	public int access(final String path, final int access) { return 0; }

	/**
	 * Method that pre-fetches the user home. 
	 * If it doesn't exist, it tries to create it.
	 */
	@Override
	public void beforeUnmount(final java.io.File mountPoint){ 
		if (this.getHomeDirectory() == null) 
		{ this.fsb.putHomeDirectory(username, new Directory("")); } 
	}

	@Override
	public int create(final String path, final ModeWrapper mode, final FileInfoWrapper info)
	{
		if (this.getHomeDirectory().find(path) != null) {
			return -ErrorCodes.EEXIST();
		}
		final Path parent = getParentPath(path);
		if (parent instanceof Directory) {
			((Directory) parent).mkfile(getLastComponent(path));
			return 0;
		}
		return -ErrorCodes.ENOENT();
	}
	
	@Override
	public int getattr(final String path, final StatWrapper stat) {
		final Path p = this.getHomeDirectory().find(path);
		if (p != null) {
			p.getattr(stat);
			return 0;
		}
		return -ErrorCodes.ENOENT();
	}

	@Override
	public int mkdir(final String path, final ModeWrapper mode) {
		if (this.getHomeDirectory().find(path) != null) 
		{ return -ErrorCodes.EEXIST(); }
		final Path parent = this.getParentPath(path);
		if (parent instanceof Directory) {
			((Directory) parent).mkdir(getLastComponent(path));
			return 0;
		}
		return -ErrorCodes.ENOENT();
	}
	
	@Override
	public int open(final String path, final FileInfoWrapper info)
	{ return 0; }
	
	@Override
	public int read(
			final String path, 
			final ByteBuffer buffer, 
			final long size, 
			final long offset, 
			final FileInfoWrapper info) {
		final Path p = this.getHomeDirectory().find(path);
		if (p == null) { return -ErrorCodes.ENOENT(); }
		if (!(p instanceof File)) { return -ErrorCodes.EISDIR(); }
		return ((File) p).read(buffer, size, offset);
	}
	
	@Override
	public int readdir(final String path, final DirectoryFiller filler)	{
		final Path p = this.getHomeDirectory().find(path);
		if (p == null) { return -ErrorCodes.ENOENT(); }
		if (!(p instanceof Directory)) { return -ErrorCodes.ENOTDIR(); }
		((Directory) p).read(filler);
		return 0;
	}
	
	@Override
	public int rename(final String path, final String newName) {
		final Path p = this.getHomeDirectory().find(path);
		if (p == null) { return -ErrorCodes.ENOENT(); }
		final Path newParent = getParentPath(newName);
		if (newParent == null) { return -ErrorCodes.ENOENT(); }
		if (!(newParent instanceof Directory)) { return -ErrorCodes.ENOTDIR(); }
		p.delete();
		p.rename(newName.substring(newName.lastIndexOf("/")));
		((Directory) newParent).add(p);
		return 0;
	}

	@Override
	public int rmdir(final String path)	{
		final Path p = this.getHomeDirectory().find(path);
		if (p == null) { return -ErrorCodes.ENOENT(); }
		if (!(p instanceof Directory)) { return -ErrorCodes.ENOTDIR(); }
		p.delete();
		return 0;
	}
	
	@Override
	public int truncate(final String path, final long offset) {
		final Path p = this.getHomeDirectory().find(path);
		if (p == null) { return -ErrorCodes.ENOENT(); }
		if (!(p instanceof File)) { return -ErrorCodes.EISDIR(); }
		((File) p).truncate(offset);
		return 0;
	}
	
	@Override
	public int unlink(final String path) {
		final Path p = this.getHomeDirectory().find(path);
		if (p == null) { return -ErrorCodes.ENOENT(); }
		p.delete();
		return 0;
	}
	
	@Override
	public int write(
			final String path, 
			final ByteBuffer buf, 
			final long bufSize, 
			final long writeOffset,
			final FileInfoWrapper wrapper) {
		final Path p = this.getHomeDirectory().find(path);
		if (p == null) { return -ErrorCodes.ENOENT(); }
		if (!(p instanceof File)) { return -ErrorCodes.EISDIR(); }
		return ((File) p).write(buf, bufSize, writeOffset);
	}
	
}
