package de.mas.wiiu.jnus.fuse_wiiu.implementation;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Optional;

import de.mas.wiiu.jnus.NUSTitle;
import de.mas.wiiu.jnus.NUSTitleLoaderFST;
import de.mas.wiiu.jnus.NUSTitleLoaderWumad;
import de.mas.wiiu.jnus.entities.fst.FSTEntry;
import de.mas.wiiu.jnus.fuse_wiiu.Settings;
import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseDirectory;
import de.mas.wiiu.jnus.implementations.FSTDataProviderNUSTitle;
import de.mas.wiiu.jnus.interfaces.FSTDataProvider;
import de.mas.wiiu.jnus.utils.FSTUtils;

public class WUMADFuseContainer extends GroupFuseContainer {
    private final File file;

    public WUMADFuseContainer(Optional<FuseDirectory> parent, File c) {
        super(parent);
        this.file = c;
    }

    @Override
    protected void doInit() {

        FSTDataProvider dp;
        try {
            dp = new FSTDataProviderNUSTitle(getNUSTitle());

            this.addFuseContainer("[EMULATED] [P01]", new FSTDataProviderContainer(Optional.of(this), () -> {
                try {
                    return dp;
                } catch (Exception e1) {
                    e1.printStackTrace();
                    return null;
                }
            }));

            GroupFuseContainer children = new GroupFuseContainerDefault(Optional.of(this));
            this.addFuseContainer("[EMULATED] [P01] [EXTRA] ", children);

            for (FSTEntry tmd : FSTUtils.getFSTEntriesByRegEx(dp.getRoot(), ".*tmd")) {
                Optional<FSTEntry> parentOpt = tmd.getParent();
                if (parentOpt.isPresent()) {
                    FSTEntry parent = parentOpt.get();
                    if (parent.getFileChildren().stream().filter(f -> f.getFilename().endsWith(".app")).findAny().isPresent()) {
                        FSTDataProvider fdp = null;
                        try {
                            fdp = new FSTDataProviderNUSTitle(NUSTitleLoaderFST.loadNUSTitle(dp, parent, Settings.retailCommonKey));
                        } catch (IOException | ParseException e) {
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

                        children.addFuseContainer("[P01] [DECRYPTED] [" + dp.getName() + "] " + parent.getFilename(),
                                new FSTDataProviderContainer(getParent(), fdpCpy));
                    }
                }
            }

        } catch (IOException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        } catch (ParseException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }

    }

    private NUSTitle getNUSTitle() throws IOException, ParseException {
        NUSTitle t = null;
        try {
            try {
                t = NUSTitleLoaderWumad.loadNUSTitle(file, Settings.retailCommonKey);
            } catch (ParseException e) {
                t = NUSTitleLoaderWumad.loadNUSTitle(file, Settings.devCommonKey);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Ignoring " + file.getAbsolutePath() + " :" + e.getClass().getName() + " " + e.getMessage());
        }

        return t;
    }

}
