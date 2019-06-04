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

public class WUDMountedFuseContainer extends RecursivePartitionFuseContainer<WUDInfo> {

    public WUDMountedFuseContainer(Optional<FuseDirectory> parent, File input) {
        super(parent, input);
    }

    @Override
    protected List<FSTDataProvider> getDataProvider(WUDInfo info) {
        List<FSTDataProvider> dps = new ArrayList<>();
        try {
            dps = WUDLoader.getPartitonsAsFSTDataProvider(info, Settings.retailCommonKey);
        } catch (Exception e) {
            try {
                dps = WUDLoader.getPartitonsAsFSTDataProvider(info, Settings.devCommonKey);
            } catch (IOException | ParseException e1) {
                return dps;
            }
        }
        return dps;
    }

    @Override
    protected Optional<WUDInfo> loadInfo(File input) {
        return WUDUtils.loadWUDInfo(input);
    }

}
