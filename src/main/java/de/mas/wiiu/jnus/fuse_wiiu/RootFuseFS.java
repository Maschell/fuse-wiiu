package de.mas.wiiu.jnus.fuse_wiiu;

import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseContainer;
import jnr.ffi.Pointer;
import jnr.ffi.types.off_t;
import jnr.ffi.types.size_t;
import ru.serce.jnrfuse.FuseFillDir;
import ru.serce.jnrfuse.FuseStubFS;
import ru.serce.jnrfuse.struct.FileStat;
import ru.serce.jnrfuse.struct.FuseFileInfo;

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
