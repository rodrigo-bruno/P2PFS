package p2pfs.filesystem;

import java.io.File;
import java.nio.ByteBuffer;

import net.fusejna.DirectoryFiller;
import net.fusejna.FlockCommand;
import net.fusejna.FuseFilesystem;
import net.fusejna.StructFlock.FlockWrapper;
import net.fusejna.StructFuseFileInfo.FileInfoWrapper;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.StructStatvfs.StatvfsWrapper;
import net.fusejna.StructTimeBuffer.TimeBufferWrapper;
import net.fusejna.XattrListFiller;
import net.fusejna.types.TypeMode.ModeWrapper;

public class Fuse extends FuseFilesystem {

	@Override
	public int access(String arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void afterUnmount(File arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeUnmount(File arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int bmap(String arg0, FileInfoWrapper arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int chmod(String arg0, ModeWrapper arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int chown(String arg0, long arg1, long arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int create(String arg0, ModeWrapper arg1, FileInfoWrapper arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int fgetattr(String arg0, StatWrapper arg1, FileInfoWrapper arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int flush(String arg0, FileInfoWrapper arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int fsync(String arg0, int arg1, FileInfoWrapper arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int fsyncdir(String arg0, int arg1, FileInfoWrapper arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int ftruncate(String arg0, long arg1, FileInfoWrapper arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String[] getOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getattr(String arg0, StatWrapper arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getxattr(String arg0, String arg1, ByteBuffer arg2, long arg3,
			long arg4) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int link(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int listxattr(String arg0, XattrListFiller arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int lock(String arg0, FileInfoWrapper arg1, FlockCommand arg2,
			FlockWrapper arg3) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int mkdir(String arg0, ModeWrapper arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int mknod(String arg0, ModeWrapper arg1, long arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void onMount(File arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int open(String arg0, FileInfoWrapper arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int opendir(String arg0, FileInfoWrapper arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int read(String arg0, ByteBuffer arg1, long arg2, long arg3,
			FileInfoWrapper arg4) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int readdir(String arg0, DirectoryFiller arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int readlink(String arg0, ByteBuffer arg1, long arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int release(String arg0, FileInfoWrapper arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int releasedir(String arg0, FileInfoWrapper arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int removexattr(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int rename(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int rmdir(String arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int setxattr(String arg0, ByteBuffer arg1, long arg2, int arg3,
			long arg4) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int statfs(String arg0, StatvfsWrapper arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int symlink(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int truncate(String arg0, long arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int unlink(String arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int utimens(String arg0, TimeBufferWrapper arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int write(String arg0, ByteBuffer arg1, long arg2, long arg3,
			FileInfoWrapper arg4) {
		// TODO Auto-generated method stub
		return 0;
	}

}
