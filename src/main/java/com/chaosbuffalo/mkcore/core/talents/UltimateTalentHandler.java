package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.util.ResourceLocation;

public class UltimateTalentHandler extends TalentTypeHandler {
    public UltimateTalentHandler(MKPlayerData playerData) {
        super(playerData);
    }

    @Override
    public void onRecordUpdated(TalentRecord record) {

        UltimateTalent talent = (UltimateTalent) record.getNode().getTalent();
//        MKCore.LOGGER.info("UltimateTalentHandler.onRecordUpdated {} {}", record, talent);

        MKAbility ability = talent.getAbility();
        if (!record.isKnown()) {
//            MKCore.LOGGER.info("UltimateTalentHandler.onRecordUpdated removing ability");
            playerData.getKnowledge().getTalentKnowledge().clearUltimate(ability);
            playerData.getKnowledge().unlearnAbility(ability.getAbilityId());
        } else {
            tryLearn(record, ability);
        }
    }

    private void tryLearn(TalentRecord record, MKAbility ability) {
//        MKCore.LOGGER.info("UltimateTalentHandler.tryLearn checking ability");
        if (!playerData.getKnowledge().knowsAbility(ability.getAbilityId())) {
//            MKCore.LOGGER.info("UltimateTalentHandler.tryLearn learning ability");
            playerData.getKnowledge().learnAbility(ability, false);
        }
    }

    @Override
    public void onRecordLoaded(TalentRecord record) {
//        MKCore.LOGGER.info("UltimateTalentHandler.onRecordLoaded {} {}", record, record.isKnown());
        if (record.isKnown()) {
            UltimateTalent talent = (UltimateTalent) record.getNode().getTalent();
            tryLearn(record, talent.getAbility());
        }
    }

    public void onSlotChanged(int index, ResourceLocation oldAbilityId, ResourceLocation newAbilityId) {
//        MKCore.LOGGER.info("UltimateTalentHandler.onSlotChanged {} {} {}", index, oldAbilityId, newAbilityId);
        if (!oldAbilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
            playerData.getKnowledge().getActionBar().removeFromHotBar(oldAbilityId);
        }
        if (!newAbilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
//            playerData.getKnowledge().getActionBar().tryPlaceOnBar(newAbilityId);
        }
    }
}
