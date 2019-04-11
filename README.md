# fuse-wiiu
fuse-wiiu is an easy way to extract data from Wii U titles in various formats.
It's compatible to:
- Title in the installable format (.tmd, .app, .h3 etc.)
- Multiple versions of a title in the installable format (.tmd, .app, .h3 etc.)
- Wii U disc images (WUD and WUX), including kiosk discs

fuse-wiiu requires Java 8 and fuse implementation thats compatible to you OS and CPU architecture.
# Setup
Before fuse-wiiu can be used the following steps are required.

## Fuse

### Linux

[`libfuse`](https://github.com/libfuse/libfuse) needs to be installed.

**Ubuntu**
```bash
sudo apt-get install libfuse-dev
``` 

### MacOS

[`osxfuse`](https://osxfuse.github.io) needs to be installed.

```bash
brew cask install osxfuse
```

### Windows

[`winfsp`](https://github.com/billziss-gh/winfsp) needs to be installed.
```batch
choco install winfsp
```

## Keys (optional)
To decrypt the titles, some keys are required.

The retail common key is expected in either `~/.wiiu/common.key` or `./common.key` in binary. It's also possible to provide it via an command line argument `-commonkey` with the key as hex-string.

The dev common key optional for most titles but is expected in either `~/.wiiu/devcommon.key` or `./devcommon.key` in binary. It's also possible to provide it via an command line argument `-devcommonkey` with the key as hex-string.

It's possible to provide the disc keys (for decrypting WUD/WUX) or titles keys (to decrypt tmd+app) in a seperate folder. This step is optional 
Disc keys are expected in `~/.wiiu/discKeys`, and should have the same basename as the WUD/WUX. Example: If a `mygame.wux` is found, a `mygame.key` will be searched in folder next the `.wux`. If it doesn't exist, it'll try to load `~/.wiiu/discKeys/mygame.key`. The `.key` is expected to contain the disc key in binary.

If a installable title doesn't provide a `title.tik`, it's possible to provide the needed title key in the folder `~/.wiiu/titleKeys`. Corresponding to the title id, a `TITLEID.key` is expected which contains the key in binary. Example: if the title `000500101004B100` doesn't have a `title.tik`, wiiu-fuse will try to load a `000500101004B100.key` from `~/.wiiu/titleKeys`.

More information can be found in "Supported formats".
# Usage
fuse-wiiu will be started from the command line and requires Java 8 and a fuse implementation (see Setup).

## Input path
The most imported argument in the `-in` argument which defines the input path. In most cases this will be a folder, but it's also possible to choose a WUD/WUX directly. If a folder was chosen as input, it will be mirrored to the mounpath, but normal files be hidden. Directories will still be as expected, and whenever a support titles can be mounted, it will be emulated as directory with the prefix `[EMULATED] `. More information about the behavious on different fileformats can be found on "Supported formats".


## Mount path
The mountpath will be set via the `-mountpath` argument and will set the target of fuse-wiiu. This can be almost any path (and a drive on Windows).
Just make sure:
- The path doesn't exist - but the parent path (if existing) DOES exist.
- (unix) The user can only mount on a mountpoint for which he has write permission
- (unitx) The mountpoint must not be a sticky directory which isn't owned by the user (like /tmp usually is)

**Example Windows:**
To mount the folder `H:/WiiU` to `Q:/` you would use something like this:
`java "-Dfile.encoding=UTF-8" -jar wiiu-fuse.jar -in H:/WiiU -mountpath Q`

To mount the folder `H:/WiiU` to `C:/mounted` you would use something like this:
`java "-Dfile.encoding=UTF-8" -jar wiiu-fuse.jar -in H:/WiiU -mountpath C:/mounted`
**Note: You may need to force Java to use the UTF-8 charset. Quoting the VM argument is need by Powershell**

**Example Unix:**
To mount the home folder to `~/test` you use something like this:
`java -jar wiiu-fuse.jar -in ~ -mountpath ~/test`


## Optional arguments.
- `-commonkey [KEY AS HEX STRING]` The Wii U retail common key. If not provided, the key will be tried to be read from ~/.wiiu/common.key` or `./common.key`. The argument has priority.
- `-devcommonkey [KEY AS HEX STRING]` The Wii U dev common key. If not provided, the key will be tried to be read from ~/.wiiu/devcommon.key` or `./devcommon.key`. The argument has priority.
- `-disckeypath [path]` Override the path where disc keys will be tried to be loaded from.
- `-titlekeypath [path]` Override the path where titles keys will be tried to be loaded from.

## Forcing the UTF-8 charset to the JVM
`java "-Dfile.encoding=UTF-8" -jar wiiu-fuse.jar` or java -Dfile.encoding=UTF-8 -jar wiiu-fuse.jar`.

# Supported formats
It's possible to use any directory as input, `fuse-wiiu` will scan will useable formats and mount them on request. If a directory wasn't used for ~5 minutes, it will be unmounted, but automatically remounted on the next access.

If a supported format is found and successfully mounted, a support starting with `[EMULATED] ` will be emulated, which will give you access to the files. The actual content and file layout may differ from format to format.

## Wii U disc images - WUD/WUX
Images of Wii U discs are saved `.wud` (or `.wux` if compressed). Every .wux or .wud will be emulated as two different directories (for image names `game.wux`).

- [EMULATED] game.wux
    - This directory is the "normal" representation of a Wii U disc image. It'll  have one subfolder for each partition. The `GM`-Partitions will be mounted and decrypted directly in the common `code, content and meta` format. The `SI` contain the ticket and tmd for `GM` partitions. All other partitions give you files in the "installable" format (tmd,app,tik). 
- [EMULATED] [EXTRA] game.wux
    - In this directory you can find some extra data (or data in a different presentation) of the disc. This includes:
        - The `GM` partitions in the "installable" format. (Folders with the prefix `[ENCRYPTED] `)
        - Mounted and decrypted titles from all partitions. (Folder with the prefix `[DECRYPTED] [PARTITIONNAME]`)
            - This includes titles from the non-`GM` partitions (like updates), and installable titles from the decrypted `GM` partitions (titles from kiosk discs).

Expected file layout:
```
game.wux (or game.wud)
(game.key)
```

For all discs (except kiosk discs), a file containing the disc key is required. This has be either in a `.key` in the same folder as the wux/wud or in `~/.wiiu/disckeypath` (with the same basename e.g `game.wux` -> `game.key`).

Multiple WUD/WUX in the same directory are possible, they won't be mounted until you open them.

## Installable format (tmd/h3/app)
Supports the

Expected file layout:
```
- title.tmd
- 0000000X.app
- 0000000X.h3
- (title.tik)
```

The `title.tik` is optional. If no ticket was found, a file `[titleID].key` (where `[titleID]` is the titleID of the .tmd)containing the key in binary (16 bytes) is expected in `~/.wiiu/titlekeypath`.

## Extended installable format (tmd/h3/app) with multiple versions.
It's possible to have to mount multiple versions of a installble title. In this case, all `.app` files are expected in the root, and a `title.tmd` for each version inside a folder `v[version]` where `[version]` is the version number.

Expected file layout:
```
v0
  - title.tmd
v16
  - title.tmd
v48
  - title.tmd
- 0000000X.app
- 0000000X.h3
- (title.tik)
```

The `title.tik` is optional. If no ticket was found, a file `[titleID].key` (where `[titleID]` is the titleID of the .tmd)containing the key in binary (16 bytes) is expected in `~/.wiiu/titlekeypath`.

# Used libraries

- [jnr-fuse](https://github.com/SerCeMan/jnr-fuse)
- [lombok](https://projectlombok.org/) (install it to your IDE)
- [JNUSLib](https://github.com/Maschell/JNUSLib)
- [commons-cli](https://commons.apache.org/proper/commons-cli/)- 