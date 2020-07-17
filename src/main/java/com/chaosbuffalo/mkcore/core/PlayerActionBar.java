package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.mojang.datafixers.Dynamic;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.ResourceLocation;

public class PlayerActionBar extends ActiveAbilityContainer implements IActiveAbilityContainer {

    public PlayerActionBar(MKPlayerData playerData) {
        super(playerData, "action_bar", AbilitySlot.Basic, GameConstants.DEFAULT_ACTIVES, GameConstants.MAX_ACTIVES);
    }

    private void checkHotBar(ResourceLocation abilityId) {
        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY))
            return;
        MKAbilityInfo info = playerData.getKnowledge().getKnownAbilityInfo(abilityId);
        if (info != null)
            return;

        MKCore.LOGGER.debug("checkHotBar({}) - bad", abilityId);
        clearAbility(abilityId);
    }

    void onPersonaSwitch() {
        getAbilities().forEach(this::checkHotBar);
    }

    public INBT serializeNBT() {
        return serialize(NBTDynamicOps.INSTANCE);
    }

    public void deserialize(INBT tag) {
        deserialize(new Dynamic<>(NBTDynamicOps.INSTANCE, tag));
    }
}
