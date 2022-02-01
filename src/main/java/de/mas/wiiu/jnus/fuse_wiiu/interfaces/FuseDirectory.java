package de.mas.wiiu.jnus.fuse_wiiu.interfaces;

import java.util.Optional;

/**
 * Representation of a directory.
 *
 * @author Maschell
 */
public interface FuseDirectory {

    /**
     * Returns the parent of this FuseDirectory.
     *
     * @return parent
     */
    Optional<FuseDirectory> getParent();
}
