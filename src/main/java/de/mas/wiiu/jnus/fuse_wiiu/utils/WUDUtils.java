package de.mas.wiiu.jnus.fuse_wiiu.utils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;

import de.mas.wiiu.jnus.WUDLoader;
import de.mas.wiiu.jnus.fuse_wiiu.Settings;
import de.mas.wiiu.jnus.implementations.wud.parser.WUDInfo;

public class WUDUtils {
    public static Optional<WUDInfo> loadWUDInfo(File file) {
        String FSfilename = file.getName();
        String basename = FilenameUtils.getBaseName(FSfilename);
        File keyFile = new File(file.getParent() + File.separator + basename + ".key");

        if (!keyFile.exists() && Settings.disckeyPath != null) {
            System.out.println(".key not found at " + keyFile.getAbsolutePath());
            keyFile = new File(Settings.disckeyPath.getAbsoluteFile() + File.separator + basename + ".key");
            if (!keyFile.exists()) {
                System.out.println(".key not found at " + keyFile.getAbsolutePath());
            }
        }

        try {
            if (keyFile.exists()) {
                return Optional.of(WUDLoader.load(file.getAbsolutePath(), keyFile));
            } else {
                System.out.println("No .key was not found. Trying dev mode.");
                return Optional.of(WUDLoader.loadDev(file.getAbsolutePath()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            System.err.println(errors);
        }

        return Optional.empty();
    }
}
