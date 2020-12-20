package de.mas.wiiu.jnus.fuse_wiiu.implementation.loader;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.mas.wiiu.jnus.WUDLoader;
import de.mas.wiiu.jnus.fuse_wiiu.Settings;
import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FSTDataProviderLoader;
import de.mas.wiiu.jnus.fuse_wiiu.utils.WUDUtils;
import de.mas.wiiu.jnus.implementations.wud.WiiUDisc;
import de.mas.wiiu.jnus.interfaces.FSTDataProvider;
import lombok.Getter;

public class WUDFSTDataProviderLoader implements FSTDataProviderLoader<WiiUDisc> {
    @Getter
    private static WUDFSTDataProviderLoader instance =  new WUDFSTDataProviderLoader();
    
    private WUDFSTDataProviderLoader() {
    }
    
    @Override
    public List<FSTDataProvider> getDataProvider(WiiUDisc info) {
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
    public Optional<WiiUDisc> loadInfo(File input) {
        return WUDUtils.loadWUDInfo(input);
    }
}
