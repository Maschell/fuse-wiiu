package de.mas.wiiu.jnus.fuse_wiiu.implementation;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.mas.wiiu.jnus.WUDLoader;
import de.mas.wiiu.jnus.fuse_wiiu.Settings;
import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseDirectory;
import de.mas.wiiu.jnus.fuse_wiiu.utils.WUDUtils;
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
        Optional<WUDInfo> WUDInfoOpt = WUDUtils.loadWUDInfo(input);
        if (WUDInfoOpt.isPresent()) {
            parseContents(WUDInfoOpt.get());
        } else {
            System.out.println("Failed to parse WUD/WUX " + input.getAbsolutePath());
        }
    }

    protected void parseContents(WUDInfo wudInfo) {
        List<FSTDataProvider> dps = new ArrayList<>();

        try {
            dps = WUDLoader.getPartitonsAsFSTDataProvider(wudInfo, Settings.retailCommonKey);

        } catch (ParseException e) {
            try {
                dps = WUDLoader.getPartitonsAsFSTDataProvider(wudInfo, Settings.devCommonKey);
            } catch (IOException | ParseException e1) {
                e.printStackTrace();
                e1.printStackTrace();
                System.out.println("Ignoring " + input.getAbsolutePath() + " :" + e1.getClass().getName() + " " + e1.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Ignoring " + input.getAbsolutePath() + " :" + e.getClass().getName() + " " + e.getMessage());
        }

        for (val dp : dps) {
            this.addFuseContainer(dp.getName(), new FSTDataProviderContainer(getParent(), dp));
        }

    }

}
