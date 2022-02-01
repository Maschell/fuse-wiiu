package de.mas.wiiu.jnus.fuse_wiiu.implementation;

import de.mas.wiiu.jnus.fuse_wiiu.interfaces.FuseDirectory;

import java.util.Optional;

/**
 * Default GroupFuseContainer implementation
 *
 * @author Maschell
 */
public class GroupFuseContainerDefault extends GroupFuseContainer {

    public GroupFuseContainerDefault(Optional<FuseDirectory> parent) {
        super(parent);
    }

    @Override
    protected void doInit() {
    }

}
