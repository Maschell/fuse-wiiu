package de.mas.wiiu.jnus.fuse_wiiu.implementation;

import de.mas.wiiu.jnus.NUSTitle;
import de.mas.wiiu.jnus.NUSTitleLoaderLocal;
import de.mas.wiiu.jnus.entities.TMD.TitleMetaData;
import de.mas.wiiu.jnus.entities.Ticket;
import de.mas.wiiu.jnus.fuse_wiiu.Settings;
import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseDirectory;
import de.mas.wiiu.jnus.fuse_wiiu.utils.TicketUtils;
import de.mas.wiiu.jnus.implementations.FSTDataProviderNUSTitle;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Optional;

public class LocalNUSTitleContainer extends GroupFuseContainer {

    private File folder;

    public LocalNUSTitleContainer(Optional<FuseDirectory> parent, File folder) {
        super(parent);
        this.folder = folder;
    }

    @Override
    protected void doInit() {
        long titleID = 0;
        short version = 0;
        try {
            TitleMetaData tmd = TitleMetaData.parseTMD(new File(folder.getAbsoluteFile() + File.separator + "title.tmd"));
            titleID = tmd.getTitleID();
            version = tmd.getTitleVersion();
        } catch (IOException | ParseException e2) {
            return;
        }

        long titleIDcpy = titleID;

        this.addFuseContainer(String.format("v%d", version), new FSTDataProviderContainer(Optional.of(this), () -> {
            NUSTitle t = null;

            Optional<Ticket> ticketOpt = TicketUtils.getTicket(folder, Settings.titlekeyPath, titleIDcpy, Settings.retailCommonKey);
            if (!ticketOpt.isPresent()) {
                return null;
            }
            Ticket ticket = ticketOpt.get();

            try {
                t = NUSTitleLoaderLocal.loadNUSTitle(folder.getAbsolutePath(), ticket);
            } catch (Exception e) {
                ticket = Ticket.createTicket(ticket.getEncryptedKey(), titleIDcpy, Settings.devCommonKey);
                try {
                    t = NUSTitleLoaderLocal.loadNUSTitle(folder.getAbsolutePath(), ticket);
                } catch (Exception e1) {
                    e.printStackTrace();
                    e1.printStackTrace();
                }
            }

            try {
                return new FSTDataProviderNUSTitle(t);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }));
    }

}
