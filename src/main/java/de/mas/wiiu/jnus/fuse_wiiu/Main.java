package de.mas.wiiu.jnus.fuse_wiiu;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import de.mas.wiiu.jnus.fuse_wiiu.implementation.GroupFuseContainer;
import de.mas.wiiu.jnus.fuse_wiiu.implementation.GroupFuseContainerDefault;
import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseContainer;
import de.mas.wiiu.jnus.fuse_wiiu.utils.FuseContainerWrapper;
import de.mas.wiiu.jnus.utils.HashUtil;
import de.mas.wiiu.jnus.utils.Utils;
import jnr.ffi.Platform;

public class Main {
    private static final String DEV_COMMON_KEY = "devcommon.key";
    private static final String COMMON_KEY = "common.key";
    private final static String OPTION_HELP = "help";
    private final static String OPTION_MOUNTPATH = "mountpath";
    private final static String OPTION_INPUT = "in";
    private final static String OPTION_DISCKEYS = "disckeypath";
    private final static String OPTION_TITLEKEYS = "titlekeypath";
    private static final String OPTION_COMMON_KEY = "commonkey";
    private static final String OPTION_DEV_COMMON_KEY = "devcommonkey";
    private static final String HOMEPATH = System.getProperty("user.home") + File.separator + ".wiiu";
    private static final String DISC_KEY_PATH = "discKeys";
    private static final String TITLE_KEY_PATH = "titleKeys";

    private static Optional<byte[]> readKey(File file) {
        if (file.isFile()) {
            byte[] key;
            try {
                key = Files.readAllBytes(file.toPath());
                if (key != null && key.length == 16) {
                    return Optional.of(key);
                }
            } catch (IOException e) {
            }
        }
        return Optional.empty();
    }

    private static void checkKeysForFolder(File folder) {
        if (folder.exists()) {
            File commonkeyFile = new File(folder.getAbsolutePath() + File.separator + COMMON_KEY);
            File commonkeyDevFile = new File(folder.getAbsolutePath() + File.separator + DEV_COMMON_KEY);

            if (commonkeyFile.exists()) {
                readKey(commonkeyFile).ifPresent(key -> Settings.retailCommonKey = key);
            }

            if (commonkeyDevFile.exists()) {
                readKey(commonkeyDevFile).ifPresent(key -> Settings.devCommonKey = key);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (!Charset.defaultCharset().toString().equals("UTF-8")) {
            System.err.println("This application needs to be started with the \"UTF-8\" charset.");
            System.out.println("Use the jvm argument \"-Dfile.encoding=UTF-8\".");
            System.exit(-1);
        }
        File homewiiufolder = new File(HOMEPATH);

        checkKeysForFolder(homewiiufolder);
        checkKeysForFolder(new File("."));

        Options options = getOptions();

        if (args.length == 0) {
            showHelp(options);
            return;
        }

        Settings.disckeyPath = new File(HOMEPATH + File.separator + DISC_KEY_PATH);
        Settings.titlekeyPath = new File(HOMEPATH + File.separator + TITLE_KEY_PATH);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;

        String driveLetter = "Y";

        cmd = parser.parse(options, args);

        String inputPath = "";

        if (cmd.hasOption(OPTION_MOUNTPATH)) {
            driveLetter = cmd.getOptionValue(OPTION_MOUNTPATH);
        }

        if (cmd.hasOption(OPTION_INPUT)) {
            inputPath = cmd.getOptionValue(OPTION_INPUT);
        }

        if (cmd.hasOption(OPTION_COMMON_KEY)) {
            String commonKey = cmd.getOptionValue(OPTION_COMMON_KEY);
            byte[] key = Utils.StringToByteArray(commonKey);
            if (key != null && key.length == 0x10) {
                Settings.retailCommonKey = key;
                System.out.println("Common key was set from command line.");
            }
        }

        if (cmd.hasOption(OPTION_DEV_COMMON_KEY)) {
            String devCommonKey = cmd.getOptionValue(OPTION_DEV_COMMON_KEY);
            byte[] key = Utils.StringToByteArray(devCommonKey);
            if (key != null && key.length == 0x10) {
                Settings.devCommonKey = key;
                System.out.println("Dev common key was set from command line.");
            }
        }

        if (cmd.hasOption(OPTION_DISCKEYS)) {
            Settings.disckeyPath = new File(cmd.getOptionValue(OPTION_DISCKEYS));
        }

        if (cmd.hasOption(OPTION_TITLEKEYS)) {
            Settings.titlekeyPath = new File(cmd.getOptionValue(OPTION_TITLEKEYS));
        }

        if (!Arrays.equals(HashUtil.hashSHA1(Settings.retailCommonKey), Settings.retailCommonKeyHash)) {
            System.err.println("WARNING: Retail common key is not as expected");
        } else {
            System.out.println("retail common key is okay");
        }

        if (!Arrays.equals(HashUtil.hashSHA1(Settings.devCommonKey), Settings.devCommonKeyHash)) {
            System.err.println("WARNING: Dev common key is not as expected");
        } else {
            System.out.println("dev common key is okay");
        }

        System.out.println("disc key path is: " + Settings.disckeyPath.getAbsolutePath());
        System.out.println("title key path is: " + Settings.titlekeyPath.getAbsolutePath());

        GroupFuseContainer root = new GroupFuseContainerDefault(Optional.empty());

        File input = new File(inputPath);
        Map<String, FuseContainer> containers = FuseContainerWrapper.createFuseContainer(Optional.of(root), input);
        for (Entry<String, FuseContainer> c : containers.entrySet()) {
            String name = c.getKey();
            if (name.isEmpty()) {
                name = input.getAbsolutePath().replaceAll("[\\\\/:*?\"<>|]", "");
            }
            root.addFuseContainer(name, c.getValue());
        }

        RootFuseFS stub = new RootFuseFS(root);
        try {
            String path;
            switch (Platform.getNativePlatform().getOS()) {
            case WINDOWS:
                path = driveLetter + ":\\";
                break;
            default:
                path = "/tmp/mnt_wiiu_" + driveLetter;
                new File(path).mkdirs();
            }
            System.out.println("Mounting " + new File(inputPath).getAbsolutePath() + " to " + path);
            stub.mount(Paths.get(path), true, true);
        } finally {
            stub.umount();
        }
    }

    private static Options getOptions() {
        Options options = new Options();
        options.addOption(Option.builder(OPTION_MOUNTPATH).required().hasArg().desc("On Windows: the target drive letter. Unix: will be mounted to \"/tmp/mnt_wiiu_[MOUNTPATH]\"").build());
        options.addOption(Option.builder(OPTION_INPUT).required().hasArg().desc("input path").build());
        options.addOption(Option.builder(OPTION_DISCKEYS).optionalArg(true).hasArg()
                .desc("Path of .key files used to decrypt WUD/WUX. If not set \"" + HOMEPATH + File.separator + DISC_KEY_PATH + "\" will be used.").build());
        options.addOption(Option.builder(OPTION_TITLEKEYS).optionalArg(true).hasArg()
                .desc("Path of [TITLTEID].key files used to decrypt encrypted titles (.app,.tmd etc.). If not set \"" + HOMEPATH + File.separator
                        + TITLE_KEY_PATH + "\" will be used.")
                .build());
        options.addOption(Option.builder(OPTION_COMMON_KEY).optionalArg(true).hasArg()
                .desc("Wii U retail common key as binary string. Will be used even if a key is specified in \"" + HOMEPATH + File.separator + COMMON_KEY
                        + "\" or \"" + HOMEPATH + File.separator + COMMON_KEY + "\"")
                .build());
        options.addOption(Option.builder(OPTION_DEV_COMMON_KEY).optionalArg(true).hasArg()
                .desc("Wii U dev common key as binary string. Will be used even if a key is specified in \"" + HOMEPATH + File.separator + DEV_COMMON_KEY
                        + "\" or \"" + HOMEPATH + File.separator + DEV_COMMON_KEY + "\"")
                .build());

        options.addOption(OPTION_HELP, false, "shows this text");

        return options;
    }

    private static void showHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(100);
        formatter.printHelp(" ", options);
    }

}
