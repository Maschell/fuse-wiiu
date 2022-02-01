package de.mas.wiiu.jnus.fuse_wiiu.implementation;

import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseContainer;
import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseDirectory;
import de.mas.wiiu.jnus.fuse_wiiu.utils.FuseContainerWrapper;
import lombok.val;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

/**
 * Representation of a directory on the OS filesystem. For every children of this directory the FuseContainerWrapper is used to create children if needed.
 * 
 * @author Maschell
 *
 */
public class FSFuseContainer extends GroupFuseContainer {
    private final File curDir;
    private final Timer timer = new Timer();

    public FSFuseContainer(Optional<FuseDirectory> parent, File input) {
        super(parent);
        this.curDir = input;

        // Check every 5 minutes if the children of this directory have been accessed in the last 5 minutes.
        timer.schedule(new TimerTask() {
            public void run() {
                removeUnused(5 * 60 * 1000);
            }
        }, 5 * 60 * 1000, 5 * 60 * 1000);

    }

    @Override
    public void deinit() {
        // Stop the timers so this can be collected by the GC.
        timer.cancel();
        timer.purge();
    }

    Map<File, Collection<String>> existingFiles = new HashMap<>();

    /**
     * Add FuseContainer for the children of this directory, but only if they are missing.
     */
    private void updateFolder() {
        for (File f : curDir.listFiles()) {
            Collection<String> t = existingFiles.get(f);
            if (t != null && !t.isEmpty()) {
                boolean missing = false;
                for (String cur : t) {
                    if (!hasFuseContainer(cur)) {
                        missing = true;
                        break;
                    }
                }
                if (missing) {
                    for (String cur : t) {
                        removeFuseContainer(cur);
                    }
                    existingFiles.remove(f);
                } else {
                    continue;
                }
            }

            val fuseContainers = FuseContainerWrapper.createFuseContainer(Optional.of(this), f);

            for (Entry<String, FuseContainer> e : fuseContainers.entrySet()) {
                addFuseContainer(e.getKey(), e.getValue());
            }
            existingFiles.put(f, fuseContainers.keySet());
        }
    }

    @Override
    protected void doInit() {
        updateFolder();
    }

}
