package de.mas.wiiu.jnus.fuse_wiiu.implementation;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.mas.wiiu.jnus.WumadLoader;
import de.mas.wiiu.jnus.fuse_wiiu.Settings;
import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseDirectory;
import de.mas.wiiu.jnus.implementations.wud.wumad.WumadInfo;
import de.mas.wiiu.jnus.interfaces.FSTDataProvider;

public class WUMADFuseContainer extends PartitionFuseContainer<WumadInfo> {

    public WUMADFuseContainer(Optional<FuseDirectory> parent, File c) {
        super(parent, c);
    }

    @Override
    protected Optional<WumadInfo> loadInfo(File input) {
        if (input != null && input.exists()) {
            try {
                return Optional.of(WumadLoader.load(input));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    @Override
    protected List<FSTDataProvider> getDataProvider(WumadInfo info) {
        List<FSTDataProvider> dps = new ArrayList<>();
        try {
            dps = WumadLoader.getPartitonsAsFSTDataProvider(info, Settings.retailCommonKey);
        } catch (Exception e) {
            try {
                dps = WumadLoader.getPartitonsAsFSTDataProvider(info, Settings.devCommonKey);
            } catch (IOException | ParseException e1) {
                return dps;
            }
        }
        return dps;
    }

}
