package de.mas.wiiu.jnus.fuse_wiiu;

import com.kenai.jffi.MemoryIO;

import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseContainer;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.ffi.types.dev_t;
import jnr.ffi.types.gid_t;
import jnr.ffi.types.mode_t;
import jnr.ffi.types.off_t;
import jnr.ffi.types.size_t;
import jnr.ffi.types.u_int32_t;
import jnr.ffi.types.uid_t;
import ru.serce.jnrfuse.ErrorCodes;
import ru.serce.jnrfuse.FuseFillDir;
import ru.serce.jnrfuse.FuseStubFS;
import ru.serce.jnrfuse.NotImplemented;
import ru.serce.jnrfuse.flags.FuseBufFlags;
import ru.serce.jnrfuse.struct.FileStat;
import ru.serce.jnrfuse.struct.Flock;
import ru.serce.jnrfuse.struct.FuseBuf;
import ru.serce.jnrfuse.struct.FuseBufvec;
import ru.serce.jnrfuse.struct.FuseFileInfo;
import ru.serce.jnrfuse.struct.FusePollhandle;
import ru.serce.jnrfuse.struct.Statvfs;
import ru.serce.jnrfuse.struct.Timespec;

public class RootFuseFS extends FuseStubFS {

    private final FuseContainer root;

    public RootFuseFS(FuseContainer root) {
        this.root = root;
    }

    @Override
    public int getattr(String path, FileStat stat) {
        int res = root.getattr(path, stat);
        // System.out.println("getattr " + res + " for " + path);
        return res;
    }

    @Override
    public int open(String path, FuseFileInfo fi) {
        int res = root.open(path, fi);
        // System.out.println("readdir " + res + " for " + path);
        return res;
    }

    @Override
    public int readdir(String path, Pointer buf, FuseFillDir filter, @off_t long offset, FuseFileInfo fi) {
        int res = root.readdir(path, buf, filter, offset, fi);
        // System.out.println("readdir " + res + " for " + path);
        return res;
    }

    @Override
    public int read(String path, Pointer buf, @size_t long size, @off_t long offset, FuseFileInfo fi) {
        int res = root.read(path, buf, size, offset, fi);
        // System.out.println("read " + res + " for " + path);
        return res;
    }
}
