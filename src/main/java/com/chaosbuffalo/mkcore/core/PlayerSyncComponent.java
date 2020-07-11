package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.sync.ISyncObject;
import com.chaosbuffalo.mkcore.sync.SyncGroup;
import com.chaosbuffalo.mkcore.sync.UpdateEngine;

import java.util.ArrayList;
import java.util.List;

public class PlayerSyncComponent {

    private final SyncGroup publicUpdater = new SyncGroup();
    private final SyncGroup privateUpdater = new SyncGroup();
    private final List<PlayerSyncComponent> children = new ArrayList<>();

    public PlayerSyncComponent() {
    }

    public PlayerSyncComponent(String name) {
        publicUpdater.setNestingName(name);
        privateUpdater.setNestingName(name);
    }

    public void attach(UpdateEngine engine) {
        engine.addPublic(publicUpdater);
        engine.addPrivate(privateUpdater);
        children.forEach(c -> c.attach(engine));
    }

    public void detach(UpdateEngine engine) {
        engine.removePublic(publicUpdater);
        engine.removePrivate(privateUpdater);
        children.forEach(c -> c.detach(engine));
    }

    public void addChild(PlayerSyncComponent component) {
        children.add(component);
    }

    public void addPublic(ISyncObject syncObject) {
        publicUpdater.add(syncObject);
    }

    public void addPrivate(ISyncObject syncObject) {
        addPrivate(syncObject, false);
    }

    public void addPrivate(ISyncObject syncObject, boolean forceUpdate) {
        privateUpdater.add(syncObject);
        if (forceUpdate) {
            privateUpdater.forceDirty();
        }
    }
}
