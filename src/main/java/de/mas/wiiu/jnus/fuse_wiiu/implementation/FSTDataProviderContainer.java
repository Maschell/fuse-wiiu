package de.mas.wiiu.jnus.fuse_wiiu.implementation;

import de.mas.wiiu.jnus.entities.FST.nodeentry.DirectoryEntry;
import de.mas.wiiu.jnus.entities.FST.nodeentry.FileEntry;
import de.mas.wiiu.jnus.entities.FST.nodeentry.NodeEntry;
import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseContainer;
import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseDirectory;
import de.mas.wiiu.jnus.interfaces.FSTDataProvider;
import de.mas.wiiu.jnus.utils.FSTUtils;
import jnr.ffi.Pointer;
import lombok.Getter;
import ru.serce.jnrfuse.ErrorCodes;
import ru.serce.jnrfuse.FuseFillDir;
import ru.serce.jnrfuse.struct.FileStat;
import ru.serce.jnrfuse.struct.FuseFileInfo;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * FuseContainer implementation based on a FSTDataProvider.
 * 
 * @author Maschell
 *
 */
public class FSTDataProviderContainer implements FuseContainer {
    private final Optional<FuseDirectory> parent;
    @Getter(lazy = true) private final FSTDataProvider dataProvider = dataProviderSupplier.get();
    private final Supplier<FSTDataProvider> dataProviderSupplier;

    public FSTDataProviderContainer(Optional<FuseDirectory> parent, FSTDataProvider dp) {
        this(parent, () -> dp);
    }

    public FSTDataProviderContainer(Optional<FuseDirectory> parent, Supplier<FSTDataProvider> dp) {
        this.parent = parent;
        this.dataProviderSupplier = dp;
    }

    @Override
    public Optional<FuseDirectory> getParent() {
        return parent;
    }

    @Override
    public int open(String path, FuseFileInfo fi) {
        Optional<NodeEntry> entryOpt = FSTUtils.getFSTEntryByFullPath(getDataProvider().getRoot(), path);
        if (entryOpt.isPresent()) {
            if (entryOpt.get().isDirectory()) {
                return -ErrorCodes.EISDIR();
            } else if (!entryOpt.get().isLink()) {
                return 0;
            }
        }
        return -ErrorCodes.ENOENT();
    }

    @Override
    public int getattr(String path, FileStat stat) {
        if (path.equals("/")) {
            stat.st_mode.set(FileStat.S_IFDIR | 0755);
            stat.st_nlink.set(2);
            return 0;
        }
        Optional<NodeEntry> entryOpt = FSTUtils.getFSTEntryByFullPath(getDataProvider().getRoot(), path);

        int res = 0;
        if (entryOpt.isPresent()) {
            NodeEntry entry = entryOpt.get();
            if (entry.isDirectory()) {
                stat.st_mode.set(FileStat.S_IFDIR | 0755);
                stat.st_nlink.set(2);
            } else {
                stat.st_mode.set(FileStat.S_IFREG | FileStat.ALL_READ);
                stat.st_nlink.set(1);
                stat.st_size.set(((FileEntry) entry).getSize());
            }
        } else {
            System.out.println("error for " + path);
            return -ErrorCodes.ENOENT();
        }
        return res;
    }

    @Override
    public int readdir(String path, Pointer buf, FuseFillDir filter, long offset, FuseFileInfo fi) {
        DirectoryEntry entry = getDataProvider().getRoot();

        if (!path.equals("/")) {
            Optional<DirectoryEntry> entryOpt = FSTUtils.getFileEntryDir(entry, path);
            if (!entryOpt.isPresent()) {
                return -ErrorCodes.ENOENT();
            }
            entry = entryOpt.get();
        }

        filter.apply(buf, ".", null, 0);
        filter.apply(buf, "..", null, 0);

        for (NodeEntry e : entry.getChildren()) {
            if (!e.isLink()) {
                filter.apply(buf, e.getName(), null, 0);
            }
        }
        return 0;
    }

    @Override
    public int read(String path, Pointer buf, long size, long offset, FuseFileInfo fi) {
        Optional<NodeEntry> entryopt = FSTUtils.getFSTEntryByFullPath(getDataProvider().getRoot(), path);
        if (entryopt.isPresent() && !entryopt.get().isLink() && entryopt.get().isFile()) {

            FileEntry entry = (FileEntry) entryopt.get();

            if (offset >= entry.getSize()) {
                return 0;
            }
            if (offset + size > entry.getSize()) {
                size = entry.getSize() - offset;
            }

            if (size > Integer.MAX_VALUE) {
                System.err.println("Request read size was too big.");
                return -ErrorCodes.EIO();
            }

            try {
                byte[] data;
                if (offset % 16 > 0) {
                    // make sure the offset is aligned to 0x10;
                    // in worst case we read 15 additional bytes-
                    long newOffset = (offset / 16) * 16;
                    int diff = (int) (offset - newOffset);
                    data = getDataProvider().readFile(entry, newOffset, size + diff);

                    buf.put(0, data, diff, data.length - diff);

                    return (int) (data.length > size ? size : data.length);
                } else {
                    data = getDataProvider().readFile(entry, offset, size);
                    buf.put(0, data, 0, data.length);
                    return data.length;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return -ErrorCodes.ENOENT();
            }
        } else {
            System.out.println("Path not found:" + path);
            return -ErrorCodes.ENOENT();
        }
    }

    @Override
    public void init() {
    }

    @Override
    public void deinit() {
    }

}
