package de.mas.wiiu.jnus.fuse_wiiu.implementation;

import java.io.File;
import java.util.Optional;

import de.mas.wiiu.jnus.NUSTitleLoaderWoomy;
import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseDirectory;
import de.mas.wiiu.jnus.implementations.FSTDataProviderNUSTitle;

public class WoomyNUSTitleContainer extends GroupFuseContainer {
    private final File file;

    public WoomyNUSTitleContainer(Optional<FuseDirectory> parent, File file) {
        super(parent);
        this.file = file;
    }

    @Override
    protected void doInit() {
        this.addFuseContainer(file.getName(), new FSTDataProviderContainer(Optional.of(this), () -> {
            try {
                return new FSTDataProviderNUSTitle(NUSTitleLoaderWoomy.loadNUSTitle(file.getAbsolutePath()));
            } catch (Exception e1) {
                e1.printStackTrace();
                return null;
            }
        }));

    }

}
