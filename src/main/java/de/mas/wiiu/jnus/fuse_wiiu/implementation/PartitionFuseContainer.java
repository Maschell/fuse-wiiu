package de.mas.wiiu.jnus.fuse_wiiu.implementation;

import java.io.File;
import java.util.List;
import java.util.Optional;

import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseDirectory;
import de.mas.wiiu.jnus.interfaces.FSTDataProvider;
import lombok.val;

public abstract class PartitionFuseContainer<T> extends GroupFuseContainer {
    private final File file;

    public PartitionFuseContainer(Optional<FuseDirectory> parent, File file) {
        super(parent);
        this.file = file;
    }

    @Override
    protected void doInit() {
        Optional<T> infoOpt = loadInfo(file);
        if (infoOpt.isPresent()) {
            parseContents(getDataProvider(infoOpt.get()));
        } else {
            System.err.println("Failed to parse " + file.getAbsolutePath());
        }
    }

    protected abstract Optional<T> loadInfo(File input);

    abstract protected List<FSTDataProvider> getDataProvider(T info);

    void parseContents(List<FSTDataProvider> dps) {
        for (val dp : dps) {
            this.addFuseContainer(dp.getName(), new FSTDataProviderContainer(getParent(), dp));
        }
    }
}
