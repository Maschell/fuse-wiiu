package de.mas.wiiu.jnus.fuse_wiiu.implementation;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseContainer;
import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseDirectory;
import de.mas.wiiu.jnus.fuse_wiiu.utils.WUDUtils;
import de.mas.wiiu.jnus.implementations.wud.WUDImage;
import de.mas.wiiu.jnus.implementations.wud.parser.WUDInfo;
import jnr.ffi.Pointer;
import ru.serce.jnrfuse.ErrorCodes;
import ru.serce.jnrfuse.FuseFillDir;
import ru.serce.jnrfuse.struct.FileStat;
import ru.serce.jnrfuse.struct.FuseFileInfo;

public class WUDToWUDContainer implements FuseContainer {
    private final String filename;
    private final Optional<WUDInfo> wudInfo;
    private final Optional<FuseDirectory> parent;

    public WUDToWUDContainer(Optional<FuseDirectory> parent, File c) {
        this.wudInfo = WUDUtils.loadWUDInfo(c);
        this.parent = parent;
        this.filename = c.getName().replace("_part1.", ".").replace(".wux", ".wud");
    }

    @Override
    public Optional<FuseDirectory> getParent() {
        return parent;
    }

    @Override
    public int getattr(String path, FileStat stat) {
        if (path.equals("/")) {
            stat.st_mode.set(FileStat.S_IFDIR | 0755);
            stat.st_nlink.set(2);
            return 0;
        }

        if (path.equals("/" + filename)) {
            if (stat != null) {
                stat.st_mode.set(FileStat.S_IFREG | FileStat.ALL_READ);
                stat.st_nlink.set(1);
                stat.st_size.set(WUDImage.WUD_FILESIZE);
            }
            return 0;
        }

        return -ErrorCodes.ENOENT();
    }

    @Override
    public int open(String path, FuseFileInfo fi) {
        if (path.equals("/")) {
            return -ErrorCodes.EISDIR();
        }
        return getattr(path, null);
    }

    @Override
    public int readdir(String path, Pointer buf, FuseFillDir filter, long offset, FuseFileInfo fi) {
        filter.apply(buf, ".", null, 0);
        if (getParent().isPresent()) {
            filter.apply(buf, "..", null, 0);
        }

        if (wudInfo.isPresent()) {
            filter.apply(buf, filename, null, 0);
        }
        return 0;
    }

    @Override
    public int read(String path, Pointer buf, long size, long offset, FuseFileInfo fi) {
        if (path.equals("/")) {
            return -ErrorCodes.EISDIR();
        }

        if (!path.equals("/" + filename)) {
            return -ErrorCodes.ENOENT();
        }

        if (offset >= WUDImage.WUD_FILESIZE) {
            return -ErrorCodes.ENOENT();
        }
        if (offset + size > WUDImage.WUD_FILESIZE) {
            size = WUDImage.WUD_FILESIZE - offset;
        }

        try {
            byte[] data;
            data = wudInfo.get().getWUDDiscReader().readEncryptedToByteArray(offset, 0, size);
            buf.put(0, data, 0, data.length);
            return data.length;
        } catch (IOException e) {
            e.printStackTrace();
            return -ErrorCodes.ENOENT();
        }
    }

    @Override
    public void init() {
        // Not used
    }

    @Override
    public void deinit() {
        // Not used
    }

}
