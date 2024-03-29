package de.mas.wiiu.jnus.fuse_wiiu.utils;

import de.mas.wiiu.jnus.entities.Ticket;
import de.mas.wiiu.jnus.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

public class TicketUtils {
    public static Optional<Ticket> getTicket(File folder, File keyFolder, long titleID, byte[] commonKey) {
        File ticketFile = null;
        if (folder != null) {
            ticketFile = FileUtils.getFileIgnoringFilenameCases(folder.getAbsolutePath(), "title.tik");
        }
        if (ticketFile == null) {
            ticketFile = FileUtils.getFileIgnoringFilenameCases(folder.getAbsolutePath(), "cetk");
        }
        Ticket ticket = null;
        if (ticketFile != null && ticketFile.exists()) {
            try {
                ticket = Ticket.parseTicket(ticketFile, commonKey);
            } catch (IOException e) {
            }
        }

        if (ticket == null && keyFolder != null) {
            File keyFile = FileUtils.getFileIgnoringFilenameCases(keyFolder.getAbsolutePath(), String.format("%016X", titleID) + ".key");
            if (keyFile != null && keyFile.exists()) {
                byte[] key;
                try {
                    key = Files.readAllBytes(keyFile.toPath());
                    if (key != null && key.length == 16) {
                        ticket = Ticket.createTicket(key, titleID, commonKey);
                    }
                } catch (IOException e) {
                }
            }
        }
        if (ticket != null) {
            return Optional.of(ticket);
        }
        return Optional.empty();
    }
}
