package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.core.MKPlayerData;

public abstract class TalentTypeHandler {
    protected final MKPlayerData playerData;

    public TalentTypeHandler(MKPlayerData playerData) {
        this.playerData = playerData;
    }

    public void onJoinWorld() {

    }

    public void onPersonaActivated() {

    }

    public void onPersonaDeactivated() {

    }

    public void onRecordUpdated(TalentRecord record) {

    }

    public void onRecordLoaded(TalentRecord record) {

    }
}
