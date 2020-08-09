package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.core.MKPlayerData;

import java.util.HashMap;
import java.util.Map;

public class PlayerTalentModule {
    private final MKPlayerData playerData;
    Map<TalentType<?>, TalentTypeHandler> typeHandlerMap = new HashMap<>();


    public PlayerTalentModule(MKPlayerData playerData) {
        this.playerData = playerData;
    }

    private TalentTypeHandler getRecordHandler(TalentRecord record) {
        return getTypeHandler(record.getNode().getTalentType());
    }

    public void onTalentRecordUpdated(TalentRecord record) {
        getRecordHandler(record).onRecordUpdated(record);
    }

    public <T extends TalentTypeHandler> T getTypeHandler(TalentType<T> type) {
        //noinspection unchecked
        return (T) typeHandlerMap.computeIfAbsent(type, t -> type.createTypeHandler(playerData));
    }

    public void onPersonaActivated() {
        typeHandlerMap.clear();

        playerData.getKnowledge()
                .getTalentKnowledge()
                .getKnownTalentsStream()
                .forEach(r -> getRecordHandler(r).onRecordLoaded(r));

        typeHandlerMap.values().forEach(TalentTypeHandler::onPersonaActivated);
    }

    public void onPersonaDeactivated() {
        typeHandlerMap.values().forEach(TalentTypeHandler::onPersonaDeactivated);
    }

    public void onJoinWorld() {
        typeHandlerMap.values().forEach(TalentTypeHandler::onJoinWorld);
    }
}
