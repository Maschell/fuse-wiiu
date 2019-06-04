package de.mas.wiiu.jnus.fuse_wiiu.interfaces;

import java.io.File;
import java.util.List;
import java.util.Optional;

import de.mas.wiiu.jnus.interfaces.FSTDataProvider;

public interface FSTDataProviderLoader<T> {
    public Optional<T> loadInfo(File input);

    public List<FSTDataProvider> getDataProvider(T info);
}
