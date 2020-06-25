package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.util.ResourceLocation;

public class UltimateTalentHandler extends AbilityTalentHandler {
    public UltimateTalentHandler(MKPlayerData playerData) {
        super(playerData);
    }

    @Override
    public void onSlotChanged(int index, ResourceLocation oldAbilityId, ResourceLocation newAbilityId) {
        MKCore.LOGGER.info("UltimateTalentHandler.onSlotChanged {} {} {}", index, oldAbilityId, newAbilityId);
    }
}
