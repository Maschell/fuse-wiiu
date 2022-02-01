package de.mas.wiiu.jnus.fuse_wiiu.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import de.mas.wiiu.jnus.fuse_wiiu.implementation.FSFuseContainer;
import de.mas.wiiu.jnus.fuse_wiiu.implementation.LocalBackupNUSTitleContainer;
import de.mas.wiiu.jnus.fuse_wiiu.implementation.LocalNUSTitleContainer;
import de.mas.wiiu.jnus.fuse_wiiu.implementation.MultipleFSTDataProviderFuseContainer;
import de.mas.wiiu.jnus.fuse_wiiu.implementation.MultipleFSTDataProviderRecursiveFuseContainer;
import de.mas.wiiu.jnus.fuse_wiiu.implementation.RemoteLocalBackupNUSTitleContainer;
import de.mas.wiiu.jnus.fuse_wiiu.implementation.WUDToWUDContainer;
import de.mas.wiiu.jnus.fuse_wiiu.implementation.WoomyNUSTitleContainer;
import de.mas.wiiu.jnus.fuse_wiiu.implementation.loader.WUDFSTDataProviderLoader;
import de.mas.wiiu.jnus.fuse_wiiu.implementation.loader.WumadFSTDataProviderLoader;
import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseContainer;
import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseDirectory;
import de.mas.wiiu.jnus.implementations.wud.reader.WUDDiscReaderSplitted;
import de.mas.wiiu.jnus.utils.Utils;

public class FuseContainerWrapper {

    private final static String prefix = "[EMULATED] ";

    public static Map<String, FuseContainer> createFuseContainer(Optional<FuseDirectory> parent, File c) {
        System.out.println("Mounting " + c.getAbsolutePath());

        Map<String, FuseContainer> result = new HashMap<>();
        if (c.exists() && c.isDirectory()) {
            File[] tmd = c.listFiles(f -> f.isFile() && (f.getName().startsWith("tmd.") || f.getName().startsWith("title.tmd")));
            if (tmd != null && tmd.length > 0) {
                // In case there is a tmd file

                // Checks if we have the local backup format
                File[] versions = c.listFiles(f -> f.getName().startsWith("tmd."));
                if (versions != null && versions.length > 0 && c.getName().length() == 16 && Utils.StringToLong(c.getName()) > 0) {
                    result.put(prefix + c.getName(), new LocalBackupNUSTitleContainer(parent, c));
                    return result;
                }

                // if not return normal title container.
                result.put(prefix + c.getName(), new LocalNUSTitleContainer(parent, c));
                return result;
            }
        }
        
        if (c.exists() && c.getName().endsWith(".woomy")) {            
            result.put(prefix + c.getName(), new WoomyNUSTitleContainer(parent, c));
            return result;
        }
        
        if (c.exists() && c.getName().endsWith(".wumad")) {
            result.put(prefix + c.getName(), new MultipleFSTDataProviderFuseContainer<>(parent, c, WumadFSTDataProviderLoader.getInstance()));
            result.put(prefix + "[EXTRA] " + c.getName(), new MultipleFSTDataProviderRecursiveFuseContainer<>(parent, c, WumadFSTDataProviderLoader.getInstance()));
            
            return result;
        }
       
        if (checkWUD(result, parent, c)) {
            return result;
        }

        if (c.isDirectory()) {
            result.put(c.getName(), new FSFuseContainer(parent, c));
            return result;

        }
        return result;
    }

    private static boolean checkWUD(Map<String, FuseContainer> result, Optional<FuseDirectory> parent, File c) {
        if (c.exists() && c.isFile() && (c.getName().endsWith(".wux") || c.getName().endsWith(".wud") || c.getName().endsWith(".ddi") || c.getName().endsWith(".wumada"))) {
            if (c.length() == WUDDiscReaderSplitted.WUD_SPLITTED_FILE_SIZE && !c.getName().endsWith("part1.wud")) {
                return false;
            }

            result.put(prefix + c.getName(), new MultipleFSTDataProviderFuseContainer<>(parent, c, WUDFSTDataProviderLoader.getInstance()));
            result.put(prefix + "[EXTRA] " + c.getName(), new MultipleFSTDataProviderRecursiveFuseContainer<>(parent, c, WUDFSTDataProviderLoader.getInstance()));
            if (c.getName().endsWith("part1.wud") || c.getName().endsWith(".wux")) {
                result.put(prefix + "[WUD] " + c.getName(), new WUDToWUDContainer(parent, c));
            }
            return true;

        }
        return false;
    }

}
