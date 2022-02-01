package de.mas.wiiu.jnus.fuse_wiiu.interfaces;

import jnr.ffi.Pointer;
import jnr.ffi.types.off_t;
import jnr.ffi.types.size_t;
import ru.serce.jnrfuse.FuseFillDir;
import ru.serce.jnrfuse.struct.FileStat;
import ru.serce.jnrfuse.struct.FuseFileInfo;

/**
 * Simplified version of the FuseFS interface.
 *
 * @author Maschell
 */
public interface FuseContainer extends FuseDirectory {
    /**
     * Wrapper for the getattr function of the FuseFS interface.
     * When this function is called, the path will be relative to this FuseContainer
     * <p>
     * Get file attributes.
     * <p>
     * Similar to stat().  The 'st_dev' and 'st_blksize' fields are
     * ignored.  The 'st_ino' field is ignored except if the 'use_ino'
     * mount option is given.
     */
    int getattr(String path, FileStat stat);

    /**
     * Wrapper for the getattr function of the FuseFS interface.
     * When this function is called, the path will be relative to this FuseContainer
     * <p>
     * File open operation
     * <p>
     * No creation (O_CREAT, O_EXCL) and by default also no
     * truncation (O_TRUNC) flags will be passed to open(). If an
     * application specifies O_TRUNC, fuse first calls truncate()
     * and then open(). Only if 'atomic_o_trunc' has been
     * specified and kernel version is 2.6.24 or later, O_TRUNC is
     * passed on to open.
     * <p>
     * Unless the 'default_permissions' mount option is given,
     * open should check if the operation is permitted for the
     * given flags. Optionally open may also return an arbitrary
     * filehandle in the fuse_file_info structure, which will be
     * passed to all file operations.
     *
     * @see jnr.constants.platform.OpenFlags
     */
    int open(String path, FuseFileInfo fi);

    /**
     * Wrapper for the readdir function of the FuseFS interface.
     * When this function is called, the path will be relative to this FuseContainer
     * <p>
     * Read directory
     * <p>
     * This supersedes the old getdir() interface.  New applications
     * should use this.
     * <p>
     * The filesystem may choose between two modes of operation:
     * <p>
     * 1) The readdir implementation ignores the offset parameter, and
     * passes zero to the filler function's offset.  The filler
     * function will not return '1' (unless an error happens), so the
     * whole directory is read in a single readdir operation.  This
     * works just like the old getdir() method.
     * <p>
     * 2) The readdir implementation keeps track of the offsets of the
     * directory entries.  It uses the offset parameter and always
     * passes non-zero offset to the filler function.  When the buffer
     * is full (or an error happens) the filler function will return
     * '1'.
     */
    int readdir(String path, Pointer buf, FuseFillDir filter, @off_t long offset, FuseFileInfo fi);

    /**
     * Wrapper for the getattr function of the FuseFS interface.
     * When this function is called, the path will be relative to this FuseContainer
     * <p>
     * Read data from an open file
     * <p>
     * Read should return exactly the number of bytes requested except
     * on EOF or error, otherwise the rest of the data will be
     * substituted with zeroes.  An exception to this is when the
     * 'direct_io' mount option is specified, in which case the return
     * value of the read system call will reflect the return value of
     * this operation.
     */
    int read(String path, Pointer buf, @size_t long size, @off_t long offset, FuseFileInfo fi);

    /**
     * This function will be called when ever the FuseContainer needs to update it's children.
     */
    void init();

    /**
     * This function will be called right before this FuseContainer won't be used anymore.
     */
    void deinit();


}
