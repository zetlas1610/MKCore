package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.AbilitySlotType;
import com.chaosbuffalo.mkcore.core.ActiveAbilityContainer;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.util.ResourceLocation;

public class ActiveTalentAbilityContainer extends ActiveAbilityContainer {
    protected TalentType<?> talentType;

    public ActiveTalentAbilityContainer(MKPlayerData playerData, String name, AbilitySlotType type, int defaultSize, int max, TalentType<?> talentType) {
        super(playerData, name, type, defaultSize, max);
        this.talentType = talentType;
    }

    public TalentType<?> getTalentType() {
        return talentType;
    }

    private boolean knowsTalent(ResourceLocation talentId) {
        return playerData.getKnowledge().getTalentKnowledge().getKnownTalentIds(talentType).contains(talentId);
    }

    public void setActiveTalent(int index, ResourceLocation talentId) {
        if (talentId.equals(MKCoreRegistry.INVALID_TALENT)) {
            clearSlot(index);
            return;
        }

        if (knowsTalent(talentId)) {
            MKAbility ability = TalentManager.getTalentAbility(talentId);
            if (ability == null) {
                MKCore.LOGGER.error("ActiveTalentAbilityContainer.setActiveTalent {} {} - talent does provide ability!", index, talentId);
                return;
            }

            setAbilityInSlot(index, ability.getAbilityId());
        } else {
            MKCore.LOGGER.error("ActiveTalentAbilityContainer.setActiveTalent {} {} - player does not know talent!", index, talentId);
        }
    }
}
