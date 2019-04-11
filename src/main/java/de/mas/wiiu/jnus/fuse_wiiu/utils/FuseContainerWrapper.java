package de.mas.wiiu.jnus.fuse_wiiu.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import de.mas.wiiu.jnus.fuse_wiiu.implementation.FSFuseContainer;
import de.mas.wiiu.jnus.fuse_wiiu.implementation.LocalBackupNUSTitleContainer;
import de.mas.wiiu.jnus.fuse_wiiu.implementation.LocalNUSTitleContainer;
import de.mas.wiiu.jnus.fuse_wiiu.implementation.WUDFuseContainer;
import de.mas.wiiu.jnus.fuse_wiiu.implementation.WUDMountedFuseContainer;
import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseContainer;
import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseDirectory;
import de.mas.wiiu.jnus.utils.Utils;

public class FuseContainerWrapper {

    private final static String prefix = "[EMULATED] ";

    public static Map<String, FuseContainer> createFuseContainer(Optional<FuseDirectory> parent, File c) {
        System.out.println("Mounting " + c.getAbsolutePath());
        Map<String, FuseContainer> result = new HashMap<>();
        if (c.exists() && c.isDirectory()) {
            File[] tmd = c.listFiles(f -> f.isFile() && f.getName().equals("title.tmd"));
            if (tmd != null && tmd.length > 0) {
                // In case there is a tmd file

                // Checks if we have the local backup format
                File[] versions = c.listFiles(f -> f.isDirectory() && f.getName().startsWith("v"));
                if (versions != null && versions.length > 0 && c.getName().length() == 16 && Utils.StringToLong(c.getName()) > 0) {
                    result.put(prefix + c.getName(), new LocalBackupNUSTitleContainer(parent, c));
                    return result;
                }

                // if not return normal title container.
                result.put(prefix + c.getName(), new LocalNUSTitleContainer(parent, c));
                return result;
            }
        }

        if (c.exists() && c.isFile() && (c.getName().endsWith(".wux") || c.getName().endsWith(".wud"))) {

            result.put(prefix + c.getName(), new WUDFuseContainer(parent, c));
            result.put(prefix + "[EXTRA] " + c.getName(), new WUDMountedFuseContainer(parent, c));
            return result;

        }

        if (c.isDirectory()) {
            result.put(c.getName(), new FSFuseContainer(parent, c));
            return result;

        }
        return result;
    }

}
