package de.mas.wiiu.jnus.fuse_wiiu.implementation;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;

import de.mas.wiiu.jnus.WUDLoader;
import de.mas.wiiu.jnus.fuse_wiiu.Settings;
import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseDirectory;
import de.mas.wiiu.jnus.implementations.wud.parser.WUDInfo;
import de.mas.wiiu.jnus.interfaces.FSTDataProvider;
import lombok.val;

public class WUDFuseContainer extends GroupFuseContainer {
    private final File input;

    public WUDFuseContainer(Optional<FuseDirectory> parent, File input) {
        super(parent);
        this.input = input;
    }

    @Override
    protected void doInit() {
        Optional<WUDInfo> WUDInfoOpt = loadWUDInfo(input);
        if (WUDInfoOpt.isPresent()) {
            parseContents(WUDInfoOpt.get());
        } else {
            System.out.println("Failed to parse WUD/WUX " + input.getAbsolutePath());
        }
    }

    private Optional<WUDInfo> loadWUDInfo(File file) {
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
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            System.err.println(errors);
        }

        return Optional.empty();
    }

    protected void parseContents(WUDInfo wudInfo) {
        List<FSTDataProvider> dps = new ArrayList<>();

        try {
            dps = WUDLoader.getPartitonsAsFSTDataProvider(wudInfo, Settings.retailCommonKey);

        } catch (ParseException e) {
            try {
                dps = WUDLoader.getPartitonsAsFSTDataProvider(wudInfo, Settings.devCommonKey);
            } catch (IOException | ParseException e1) {
                System.out.println("Ignoring " + input.getAbsolutePath() + " :" + e1.getClass().getName() + " " + e1.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Ignoring " + input.getAbsolutePath() + " :" + e.getClass().getName() + " " + e.getMessage());
        }

        for (val dp : dps) {
            this.addFuseContainer(dp.getName(), new FSTDataProviderContainer(getParent(), dp));
        }

    }

}
