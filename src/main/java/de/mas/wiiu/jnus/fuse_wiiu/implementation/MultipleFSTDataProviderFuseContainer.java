package de.mas.wiiu.jnus.fuse_wiiu.implementation;

import java.io.File;
import java.util.List;
import java.util.Optional;

import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FSTDataProviderLoader;
import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseDirectory;
import de.mas.wiiu.jnus.interfaces.FSTDataProvider;
import lombok.val;

public class MultipleFSTDataProviderFuseContainer<T> extends GroupFuseContainer {
    private final File file;
    private final FSTDataProviderLoader<T> loader;

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
            this.addFuseContainer(dp.getName(), new FSTDataProviderContainer(getParent(), dp));
        }
    }
}
