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

public class WUDFuseContainer extends PartitionFuseContainer<WUDInfo> {

    public WUDFuseContainer(Optional<FuseDirectory> parent, File input) {
        super(parent, input);
    }

    @Override
    protected Optional<WUDInfo> loadInfo(File input) {
        return WUDUtils.loadWUDInfo(input);
    }

    @Override
    protected List<FSTDataProvider> getDataProvider(WUDInfo info) {
        List<FSTDataProvider> dps = new ArrayList<>();

        try {
            dps = WUDLoader.getPartitonsAsFSTDataProvider(info, Settings.retailCommonKey);

        } catch (ParseException e) {
            try {
                dps = WUDLoader.getPartitonsAsFSTDataProvider(info, Settings.devCommonKey);
            } catch (IOException | ParseException e1) {
                e.printStackTrace();
                e1.printStackTrace();
                // System.out.println("Ignoring " + input.getAbsolutePath() + " :" + e1.getClass().getName() + " " + e1.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            // System.out.println("Ignoring " + input.getAbsolutePath() + " :" + e.getClass().getName() + " " + e.getMessage());
        }
        return dps;
    }
}
