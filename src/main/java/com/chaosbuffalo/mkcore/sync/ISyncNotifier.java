package com.chaosbuffalo.mkcore.sync;

public interface ISyncNotifier {
    void markDirty(ISyncObject syncObject);


    ISyncNotifier NONE = syncObject -> {

    };
}
