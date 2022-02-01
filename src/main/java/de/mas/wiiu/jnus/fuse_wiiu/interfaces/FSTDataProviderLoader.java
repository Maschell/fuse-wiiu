package de.mas.wiiu.jnus.fuse_wiiu.interfaces;

import de.mas.wiiu.jnus.interfaces.FSTDataProvider;

import java.io.File;
import java.util.List;
import java.util.Optional;

public interface FSTDataProviderLoader<T> {
    Optional<T> loadInfo(File input);

    List<FSTDataProvider> getDataProvider(T info);
}
