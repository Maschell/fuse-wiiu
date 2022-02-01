package de.mas.wiiu.jnus.fuse_wiiu.implementation;

import de.mas.wiiu.jnus.NUSTitleLoaderFST;
import de.mas.wiiu.jnus.entities.FST.nodeentry.DirectoryEntry;
import de.mas.wiiu.jnus.entities.FST.nodeentry.FileEntry;
import de.mas.wiiu.jnus.fuse_wiiu.Settings;
import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FSTDataProviderLoader;
import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseDirectory;
import de.mas.wiiu.jnus.implementations.FSTDataProviderNUSTitle;
import de.mas.wiiu.jnus.interfaces.FSTDataProvider;
import de.mas.wiiu.jnus.interfaces.HasNUSTitle;
import de.mas.wiiu.jnus.utils.FSTUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

public class MultipleFSTDataProviderRecursiveFuseContainer<T> extends MultipleFSTDataProviderFuseContainer<T> {
    public MultipleFSTDataProviderRecursiveFuseContainer(Optional<FuseDirectory> parent, File input, FSTDataProviderLoader<T> loader) {
        super(parent, input, loader);
    }

    @Override
    void parseContents(List<FSTDataProvider> dps) {
        try {
            for (FSTDataProvider dp : dps) {
                for (FileEntry tmd : FSTUtils.getFSTEntriesByRegEx(dp.getRoot(), ".*tmd")) {
                    DirectoryEntry parent = tmd.getParent();
                    if (parent.getFileChildren().stream().filter(f -> f.getName().endsWith(".app")).findAny().isPresent()) {
                        FSTDataProvider fdp = null;

                        try {
                            fdp = new FSTDataProviderNUSTitle(NUSTitleLoaderFST.loadNUSTitle(dp, parent, Settings.retailCommonKey));
                        } catch (IOException | ParseException e) {
                            try {
                                fdp = new FSTDataProviderNUSTitle(NUSTitleLoaderFST.loadNUSTitle(dp, parent, Settings.devCommonKey));
                            } catch (Exception e1) {
                                System.out.println("Ignoring " + parent.getName() + " :" + e1.getClass().getName() + " " + e1.getMessage());
                                continue;
                            }
                        } catch (Exception e) {
                            System.out.println("Ignoring " + parent.getName() + " :" + e.getClass().getName() + " " + e.getMessage());
                            continue;
                        }

                        FSTDataProvider fdpCpy = fdp;

                        this.addFuseContainer("[DECRYPTED] [" + dp.getName() + "] " + parent.getName(), new FSTDataProviderContainer(getParent(), fdpCpy));
                    }
                }

                if (dp instanceof HasNUSTitle) {
                    try {
                        this.addFuseContainer("[ENCRYPTED] " + dp.getName(), new NUSTitleEncryptedFuseContainer(getParent(), ((HasNUSTitle) dp).getNUSTitle()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
