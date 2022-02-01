package de.mas.wiiu.jnus.fuse_wiiu.implementation;

import de.mas.wiiu.jnus.NUSTitle;
import de.mas.wiiu.jnus.entities.TMD.Content;
import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseContainer;
import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseDirectory;
import jnr.ffi.Pointer;
import ru.serce.jnrfuse.ErrorCodes;
import ru.serce.jnrfuse.FuseFillDir;
import ru.serce.jnrfuse.struct.FileStat;
import ru.serce.jnrfuse.struct.FuseFileInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class NUSTitleEncryptedFuseContainer implements FuseContainer {
    private final Optional<FuseDirectory> parent;
    private final NUSTitle title;

    public NUSTitleEncryptedFuseContainer(Optional<FuseDirectory> parent, NUSTitle t) {
        this.parent = parent;
        this.title = t;
    }

    @Override
    public Optional<FuseDirectory> getParent() {
        return parent;
    }

    private Optional<Content> getContentForPath(String path) {
        if (!path.endsWith(".app") || path.length() != 12) {
            return Optional.empty();
        }

        try {
            int contentID = Integer.parseInt(path.substring(0, 8), 16);
            Content c = title.getTMD().getContentByID(contentID);
            if (c != null) {
                return Optional.of(c);
            }
        } catch (NumberFormatException e) {
        }
        return Optional.empty();
    }

    @SuppressWarnings("unused")
    private Optional<byte[]> getH3ForPath(String path) {
        if (!path.endsWith(".h3") || path.length() != 11) {
            return Optional.empty();
        }
        return getContentForPath(path.substring(0, 8) + ".app").flatMap(c -> {
            if (c.isHashed()) {
                try {
                    Optional<byte[]> hash = title.getDataProcessor().getDataProvider().getContentH3Hash(c);
                    return hash;
                } catch (IOException e) {
                }
            }
            return Optional.empty();
        });
    }

    private Optional<byte[]> getTMDforPath(String path) {
        return getFileforPath(path, "title.tmd", () -> {
            try {
                return title.getDataProcessor().getDataProvider().getRawTMD();
            } catch (IOException e) {
                return Optional.empty();
            }
        });
    }

    private Optional<byte[]> getTicketforPath(String path) {
        return getFileforPath(path, "title.tik", () -> {
            try {
                return title.getDataProcessor().getDataProvider().getRawTicket();
            } catch (IOException e) {
                return Optional.empty();
            }
        });
    }

    private Optional<byte[]> getCertforPath(String path) {
        return getFileforPath(path, "title.cert", () -> {
            try {
                return title.getDataProcessor().getDataProvider().getRawCert();
            } catch (IOException e) {
                return Optional.empty();
            }
        });
    }

    private Optional<byte[]> getFileforPath(String path, String expected, Supplier<Optional<byte[]>> func) {
        if (!path.equals(expected)) {
            return Optional.empty();
        }

        return func.get();
    }

    @Override
    public int open(String path, FuseFileInfo fi) {
        if (path.equals("/")) {
            return -ErrorCodes.EISDIR();
        }
        return getattr(path, null);
    }

    @Override
    public int getattr(String path, FileStat stat) {
        if (path.equals("/")) {
            stat.st_mode.set(FileStat.S_IFDIR | 0755);
            stat.st_nlink.set(2);
            return 0;
        }

        path = path.substring(1);

        Optional<Content> coOptional = getContentForPath(path);
        if (coOptional.isPresent()) {
            if (stat != null) {
                stat.st_mode.set(FileStat.S_IFREG | FileStat.ALL_READ);
                stat.st_nlink.set(1);
                stat.st_size.set(coOptional.get().getEncryptedFileSize());
            }
            return 0;
        } else {
            Optional<byte[]> h3Data = getH3ForPath(path);
            if (h3Data.isPresent()) {
                if (stat != null) {
                    stat.st_mode.set(FileStat.S_IFREG | FileStat.ALL_READ);
                    stat.st_nlink.set(1);
                    stat.st_size.set(h3Data.get().length);
                }
                return 0;
            }
        }

        List<Supplier<Optional<byte[]>>> functions = new ArrayList<>();

        String pathcopy = path;

        functions.add(() -> getTMDforPath(pathcopy));
        functions.add(() -> getTicketforPath(pathcopy));
        functions.add(() -> getCertforPath(pathcopy));

        for (Supplier<Optional<byte[]>> func : functions) {
            Optional<byte[]> data = func.get();
            if (data.isPresent()) {
                if (stat != null) {
                    stat.st_mode.set(FileStat.S_IFREG | FileStat.ALL_READ);
                    stat.st_nlink.set(1);
                    stat.st_size.set(data.get().length);
                }
                return 0;
            }
        }

        return -ErrorCodes.ENOENT();
    }

    @Override
    public int readdir(String path, Pointer buf, FuseFillDir filter, long offset, FuseFileInfo fi) {
        filter.apply(buf, ".", null, 0);
        if (getParent().isPresent()) {
            filter.apply(buf, "..", null, 0);
        }
        
        for (Content e : title.getTMD().getAllContents().values()) {
            filter.apply(buf, e.getFilename(), null, 0);
            if (e.isHashed()) {
                filter.apply(buf, String.format("%08X.h3", e.getID()), null, 0);
            }
        }

        if (getTMDforPath("title.tmd").isPresent()) {
            filter.apply(buf, "title.tmd", null, 0);
        }

        if (getTicketforPath("title.tik").isPresent()) {
            filter.apply(buf, "title.tik", null, 0);
        }

        if (getCertforPath("title.cert").isPresent()) {
            filter.apply(buf, "title.cert", null, 0);
        }

        return 0;
    }

    @Override
    public int read(String path, Pointer buf, long size, long offset, FuseFileInfo fi) {
        if (path.equals("/")) {
            return -ErrorCodes.EISDIR();
        }
        
        if(size > Integer.MAX_VALUE) {
            System.err.println("Request read size was too big.");
            return -ErrorCodes.EIO();
        }

        path = path.substring(1);

        Optional<Content> coOptional = getContentForPath(path);
        if (coOptional.isPresent()) {
            Content c = coOptional.get();
            if (offset >= c.getEncryptedFileSize()) {
                return -ErrorCodes.EIO();
            }
            if (offset + size > c.getEncryptedFileSize()) {
                size = c.getEncryptedFileSize() - offset;
            }

            byte[] data;
            try {
                data = title.getDataProcessor().readContent(c, offset, (int) size);
                buf.put(0, data, 0, data.length);
                return data.length;
            } catch (Exception e) {
                e.printStackTrace();
                return -ErrorCodes.ENOENT();
            }
        } else {
            Optional<byte[]> h3Data = getH3ForPath(path);
            if (h3Data.isPresent()) {
                byte[] hash = h3Data.get();

                if (offset >= hash.length) {
                    return -ErrorCodes.EIO();
                }
                if (offset + size > hash.length) {
                    size = hash.length - offset;
                }

                buf.put(0, hash, (int) offset, (int) size);
                return (int) size;
            }
        }

        // Check if the tmd ticket or cert are request.
        List<Supplier<Optional<byte[]>>> functions = new ArrayList<>();
        String pathcopy = path;
        functions.add(() -> getTMDforPath(pathcopy));
        functions.add(() -> getTicketforPath(pathcopy));
        functions.add(() -> getCertforPath(pathcopy));
        for (Supplier<Optional<byte[]>> func : functions) {
            Optional<byte[]> dataOpt = func.get();
            if (dataOpt.isPresent()) {
                byte[] data = dataOpt.get();
                if (data == null || data.length == 0) {
                    return -ErrorCodes.ENOENT();
                }
                if (offset >= data.length) {
                    return -ErrorCodes.ENOENT();
                }
                if (offset + size > data.length) {
                    size = data.length - offset;
                }
                buf.put(0, data, (int) offset, (int) size);
                return (int) size;
            }
        }

        return 0;
    }

    @Override
    public void init() {
    }

    @Override
    public void deinit() {
    }

}
