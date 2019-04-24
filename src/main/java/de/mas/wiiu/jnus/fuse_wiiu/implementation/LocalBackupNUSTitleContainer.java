package de.mas.wiiu.jnus.fuse_wiiu.implementation;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import de.mas.wiiu.jnus.NUSTitle;
import de.mas.wiiu.jnus.NUSTitleLoaderLocalBackup;
import de.mas.wiiu.jnus.entities.Ticket;
import de.mas.wiiu.jnus.fuse_wiiu.Settings;
import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseDirectory;
import de.mas.wiiu.jnus.fuse_wiiu.utils.TicketUtils;
import de.mas.wiiu.jnus.implementations.FSTDataProviderNUSTitle;
import de.mas.wiiu.jnus.utils.Utils;

public class LocalBackupNUSTitleContainer extends GroupFuseContainer {

    private File folder;

    public LocalBackupNUSTitleContainer(Optional<FuseDirectory> parent, File folder) {
        super(parent);
        this.folder = folder;
    }

    @Override
    protected void doInit() {
        File[] wud = folder.listFiles(f -> f.isDirectory() && f.getName().startsWith("v"));
        for (File versionF : wud) {
            short version = Short.parseShort(versionF.getName().substring(1));
            this.addFuseContainer(String.format("v%d", version), new FSTDataProviderContainer(Optional.of(this), () -> {
                long titleID = Utils.StringToLong(folder.getName());
                NUSTitle t = null;
                Optional<Ticket> ticketOpt = TicketUtils.getTicket(folder, Settings.titlekeyPath, titleID, Settings.retailCommonKey);
                if (!ticketOpt.isPresent()) {
                    return null;
                }
                Ticket ticket = ticketOpt.get();
                try {
                    t = NUSTitleLoaderLocalBackup.loadNUSTitle(folder.getAbsolutePath(), version, ticket);
                } catch (Exception e) {
                    // Try dev ticket
                    ticket = Ticket.createTicket(ticket.getEncryptedKey(), titleID, Settings.devCommonKey);
                    try {
                        t = NUSTitleLoaderLocalBackup.loadNUSTitle(folder.getAbsolutePath(), version, ticket);
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

}
