package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;

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

    private void dumpAttrInstance(IAttributeInstance instance) {
        MKCore.LOGGER.info("\tAttribute {}", instance.getAttribute().getName());
        for (AttributeModifier modifier : instance.func_225505_c_()) {
            MKCore.LOGGER.info("\t\tmodifier {}", modifier);
        }
    }

    private void dumpAttributes(String location) {
        MKCore.LOGGER.info("All Attributes @ {}", location);
        AbstractAttributeMap map = playerData.getEntity().getAttributes();
        map.getAllAttributes().forEach(this::dumpAttrInstance);
    }

    private void dumpDirtyAttributes(String location) {
        AttributeMap map = (AttributeMap) playerData.getEntity().getAttributes();

        MKCore.LOGGER.info("Dirty Attributes @ {}", location);
        map.getDirtyInstances().forEach(this::dumpAttrInstance);
    }


    public void onPersonaActivated() {
//        MKCore.LOGGER.info("PlayerTalentModule.onPersonaActivated");
        typeHandlerMap.clear();

        playerData.getKnowledge()
                .getTalentKnowledge()
                .getKnownTalentsStream()
                .forEach(r -> getRecordHandler(r).onRecordLoaded(r));

        typeHandlerMap.values().forEach(TalentTypeHandler::onPersonaActivated);

//        dumpDirtyAttributes("onPersonaActivated");
    }

    public void onPersonaDeactivated() {
//        MKCore.LOGGER.info("PlayerTalentModule.onPersonaDeactivated");
        typeHandlerMap.values().forEach(TalentTypeHandler::onPersonaDeactivated);
    }

    public void onJoinWorld() {
        typeHandlerMap.values().forEach(TalentTypeHandler::onJoinWorld);
    }
}
