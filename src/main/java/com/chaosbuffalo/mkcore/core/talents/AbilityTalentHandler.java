package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.util.ResourceLocation;

public abstract class AbilityTalentHandler extends TalentTypeHandler {
    public AbilityTalentHandler(MKPlayerData playerData) {
        super(playerData);
    }

    @Override
    public void onRecordUpdated(TalentRecord record) {
        MKAbility ability = TalentManager.getTalentAbility(record.getNode().getTalent().getRegistryName());
        if (ability == null)
            return;

        if (!record.isKnown()) {
            onUnknownAbilityUpdated(record, ability);
        } else {
            onKnownAbilityUpdated(record, ability);
        }
    }

    @Override
    public void onRecordLoaded(TalentRecord record) {
        if (record.isKnown()) {
            MKAbility ability = TalentManager.getTalentAbility(record.getNode().getTalent().getRegistryName());
            if (ability == null)
                return;
            tryLearn(ability);
        }
    }

    protected void onUnknownAbilityUpdated(TalentRecord record, MKAbility ability) {
        playerData.getKnowledge().unlearnAbility(ability.getAbilityId());
    }

    protected void onKnownAbilityUpdated(TalentRecord record, MKAbility ability) {
        tryLearn(ability);
    }

    protected void tryLearn(MKAbility ability) {
        if (!playerData.getKnowledge().knowsAbility(ability.getAbilityId())) {
            playerData.getKnowledge().learnAbility(ability, false);
        }
    }

    public abstract void onSlotChanged(int index, ResourceLocation oldAbilityId, ResourceLocation newAbilityId);
}
