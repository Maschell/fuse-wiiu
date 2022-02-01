package de.mas.wiiu.jnus.fuse_wiiu;

import de.mas.wiiu.jnus.utils.Utils;

import java.io.File;

public class Settings {
    public static final byte[] retailCommonKeyHash = Utils.StringToByteArray("6A0B87FC98B306AE3366F0E0A88D0B06A2813313");
    public static final byte[] devCommonKeyHash = Utils.StringToByteArray("E191BFDB1232537D7DADEAD81F2A48FD6F188E02");
    public static File disckeyPath = null;
    public static File titlekeyPath = null;
    public static byte[] retailCommonKey = new byte[16];
    public static byte[] devCommonKey = new byte[16];
}
