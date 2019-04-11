package de.mas.wiiu.jnus.fuse_wiiu.implementation;

import java.util.Optional;

import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseDirectory;

/**
 * Default GroupFuseContainer implementation
 * @author Maschell
 *
 */
public class GroupFuseContainerDefault extends GroupFuseContainer {

    public GroupFuseContainerDefault(Optional<FuseDirectory> parent) {
        super(parent);
    }

    @Override
    protected void doInit() {
    }

}
