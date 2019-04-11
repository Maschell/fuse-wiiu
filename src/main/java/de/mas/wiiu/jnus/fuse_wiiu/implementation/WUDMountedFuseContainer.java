package de.mas.wiiu.jnus.fuse_wiiu.implementation;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.mas.wiiu.jnus.NUSTitleLoaderFST;
import de.mas.wiiu.jnus.WUDLoader;
import de.mas.wiiu.jnus.entities.fst.FSTEntry;
import de.mas.wiiu.jnus.fuse_wiiu.Settings;
import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseDirectory;
import de.mas.wiiu.jnus.implementations.FSTDataProviderNUSTitle;
import de.mas.wiiu.jnus.implementations.wud.parser.WUDInfo;
import de.mas.wiiu.jnus.interfaces.FSTDataProvider;
import de.mas.wiiu.jnus.interfaces.HasNUSTitle;
import de.mas.wiiu.jnus.utils.FSTUtils;

public class WUDMountedFuseContainer extends WUDFuseContainer {

    public WUDMountedFuseContainer(Optional<FuseDirectory> parent, File input) {
        super(parent, input);
    }

    @Override
    protected void parseContents(WUDInfo wudinfo) {
        List<FSTDataProvider> dps = new ArrayList<>();
        try {
            dps = WUDLoader.getPartitonsAsFSTDataProvider(wudinfo, Settings.retailCommonKey);
        } catch (Exception e) {
            try {
                dps = WUDLoader.getPartitonsAsFSTDataProvider(wudinfo, Settings.devCommonKey);
            } catch (IOException | ParseException e1) {
                return;
            }
        }

        try {
            for (FSTDataProvider dp : dps) {
                for (FSTEntry tmd : FSTUtils.getFSTEntriesByRegEx(dp.getRoot(), ".*tmd")) {
                    FSTEntry parent = tmd.getParent();
                    if (parent.getFileChildren().stream().filter(f -> f.getFilename().endsWith(".app")).findAny().isPresent()) {
                        FSTDataProvider fdp = null;

                        try {
                            fdp = new FSTDataProviderNUSTitle(NUSTitleLoaderFST.loadNUSTitle(dp, parent, Settings.retailCommonKey));
                        } catch (ParseException e) {
                            try {
                                fdp = new FSTDataProviderNUSTitle(NUSTitleLoaderFST.loadNUSTitle(dp, parent, Settings.devCommonKey));
                            } catch (Exception e1) {
                                System.out.println("Ignoring " + parent.getFilename() + " :" + e1.getClass().getName() + " " + e1.getMessage());
                                continue;
                            }
                        } catch (Exception e) {
                            System.out.println("Ignoring " + parent.getFilename() + " :" + e.getClass().getName() + " " + e.getMessage());
                            continue;
                        }

                        FSTDataProvider fdpCpy = fdp;

                        this.addFuseContainer("[DECRYPTED] [" + dp.getName() + "] " + parent.getFilename(), new FSTDataProviderContainer(getParent(), fdpCpy));
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
