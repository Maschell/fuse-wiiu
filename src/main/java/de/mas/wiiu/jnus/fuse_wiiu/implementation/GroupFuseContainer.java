package de.mas.wiiu.jnus.fuse_wiiu.implementation;

import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseContainer;
import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseDirectory;
import jnr.ffi.Pointer;
import jnr.ffi.types.off_t;
import jnr.ffi.types.size_t;
import ru.serce.jnrfuse.ErrorCodes;
import ru.serce.jnrfuse.FuseFillDir;
import ru.serce.jnrfuse.struct.FileStat;
import ru.serce.jnrfuse.struct.FuseFileInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Implementation of an FuseContainer which can hold serveral FuseContainers emulated as directories.
 *
 * @author Maschell
 */
public abstract class GroupFuseContainer implements FuseContainer {
    private final Map<String, FuseContainer> containerMap = new HashMap<>();
    private final Map<String, Long> lastAccess = new HashMap<>();
    private final Optional<FuseDirectory> parent;

    public GroupFuseContainer(Optional<FuseDirectory> parent) {
        this.parent = parent;
    }

    /**
     * Removing old container from the list that haven't been updated in a given time frame.
     * 
     * @param duration
     * @return Number of elements that have been removed.
     */
    protected int removeUnused(long duration) {
        int count = 0;
        for (Entry<String, Long> cur : lastAccess.entrySet().stream().filter(e -> System.currentTimeMillis() - e.getValue() > duration)
                .collect(Collectors.toList())) {
            lastAccess.remove(cur.getKey());
            containerMap.remove(cur.getKey()).deinit();
            System.out.println("Unmounting " + cur.getKey());
            count++;
        }
        if (count > 0) {
            synchronized (initDone) {
                initDone = false;
            }
        }
        return count;
    }

    /**
     * 
     * @param path
     * @param func
     * @param defaultValue
     * @return
     */
    private int doForContainer(String path, BiFunction<String, FuseContainer, Integer> func, int defaultValue) {
        path.replace("\\", "/");
        path = path.substring(1);
        String[] parts = path.split("/");

        FuseContainer container = containerMap.get(parts[0]);

        if (container != null) {
            lastAccess.put(parts[0], System.currentTimeMillis());

            container.init();

            String newPath = path.substring(parts[0].length());
            if (newPath.length() == 0) {
                newPath = "/";
            }
            return func.apply(newPath, container);
        }
        return defaultValue;
    }

    @Override
    public int getattr(String path, FileStat stat) {
        path.replace("\\", "/");
        if (path.equals("/")) {
            stat.st_mode.set(FileStat.S_IFDIR | 0755);
            stat.st_nlink.set(2);
            return 0;
        }
        if (path.split("/").length == 2) {
            for (String container : containerMap.keySet()) {
                if (container.equals(path.split("/")[1])) {
                    stat.st_mode.set(FileStat.S_IFDIR | 0755);
                    stat.st_nlink.set(2);
                    return 0;
                }
            }
        }

        return doForContainer(path, (newPath, container) -> container.getattr(newPath, stat), -ErrorCodes.ENOENT());
    }

    @Override
    public int readdir(String path, Pointer buf, FuseFillDir filter, @off_t long offset, FuseFileInfo fi) {
        path.replace("\\", "/");
        if (path.equals("/")) {
            filter.apply(buf, ".", null, 0);
            if (getParent().isPresent()) {
                filter.apply(buf, "..", null, 0);
            }
            for (String container : containerMap.keySet()) {
                filter.apply(buf, container, null, 0);
            }
            return 0;
        }

        return doForContainer(path, (newPath, container) -> container.readdir(newPath, buf, filter, offset, fi), 0);
    }

    @Override
    public int read(String path, Pointer buf, @size_t long size, @off_t long offset, FuseFileInfo fi) {
        path.replace("\\", "/");
        if (path.length() <= 1) {
            return -ErrorCodes.EISDIR();
        }

        return doForContainer(path, (newPath, container) -> container.read(newPath, buf, size, offset, fi), 0);
    }

    @Override
    public int open(String path, FuseFileInfo fi) {
        path.replace("\\", "/");
        if (path.length() <= 1) {
            return -ErrorCodes.EISDIR();
        }

        return doForContainer(path, (newPath, container) -> container.open(newPath, fi), 0);
    }

    @Override
    public Optional<FuseDirectory> getParent() {
        return parent;
    }

    public FuseContainer addFuseContainer(String name, FuseContainer container) {
        return containerMap.put(name, container);
    }

    public void clearFuseContainer() {
        containerMap.clear();
    }

    public FuseContainer getFuseContainer(String name) {
        return containerMap.get(name);
    }

    public boolean hasFuseContainer(String name) {
        return containerMap.containsKey(name);
    }

    public FuseContainer removeFuseContainer(String name) {
        return containerMap.remove(name);
    }

    private Boolean initDone = false;

    @Override
    public void init() {
        synchronized (initDone) {
            if (!initDone) {
                doInit();
                initDone = true;
            }
        }
    }

    /**
     * This function is used to add FuseContainers to this GroupFuseContainer and can be called because of two reason. 1. The GroupFuseContainer is access for
     * the first time and the list FuseContainers in this group need to be added to the map using "addFuseContainer". 2. Some of the children have been removed
     * (due to inactivity). In this case the functions should add them again. So it needs to either add the missing one (check by hasFuseContainer), or
     * completely wipe and start over.
     */
    abstract protected void doInit();

    @Override
    public void deinit() {
        //
    }

}
