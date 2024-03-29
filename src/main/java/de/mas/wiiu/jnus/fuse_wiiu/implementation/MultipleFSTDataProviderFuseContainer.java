package de.mas.wiiu.jnus.fuse_wiiu.implementation;

import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FSTDataProviderLoader;
import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseDirectory;
import de.mas.wiiu.jnus.interfaces.FSTDataProvider;
import lombok.val;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class MultipleFSTDataProviderFuseContainer<T> extends GroupFuseContainer {
    private final File file;
    private final FSTDataProviderLoader<T> loader;
    private int i = 0;

    public MultipleFSTDataProviderFuseContainer(Optional<FuseDirectory> parent, File file, FSTDataProviderLoader<T> loader) {
        super(parent);
        this.file = file;
        this.loader = loader;
    }

    @Override
    protected void doInit() {
        Optional<T> infoOpt = loader.loadInfo(file);
        if (infoOpt.isPresent()) {
            parseContents(loader.getDataProvider(infoOpt.get()));
        } else {
            System.err.println("Failed to parse " + file.getAbsolutePath());
        }
    }

    void parseContents(List<FSTDataProvider> dps) {
        for (val dp : dps) {
            this.addFuseContainer(dp.getName() + "_" + (++i), new FSTDataProviderContainer(getParent(), dp));
        }
    }
}
