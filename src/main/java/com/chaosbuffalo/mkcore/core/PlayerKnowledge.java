package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.PlayerAbility;
import com.chaosbuffalo.mkcore.abilities.PlayerAbilityInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class PlayerKnowledge {

    private final MKPlayerData playerData;

    private List<ResourceLocation> hotbar = Collections.emptyList();

    public PlayerKnowledge(MKPlayerData playerData) {
        this.playerData = playerData;
    }

    private PlayerEntity getPlayer() {
        return playerData.getPlayer();
    }

    public int getActionBarSize() {
        // TODO: expandable
        return GameConstants.CLASS_ACTION_BAR_SIZE;
    }

    public ResourceLocation getAbilityInSlot(int slot) {
        if (slot < hotbar.size()) {
            return hotbar.get(slot);
        }
        return MKCoreRegistry.INVALID_ABILITY;
    }


    // TODO: temp api
    void setHotBar(List<ResourceLocation> hotBar) {
        this.hotbar = hotBar;
    }

    @Nullable
    public PlayerAbilityInfo getAbilityInfo(ResourceLocation abilityId) {
        // TODO: player knowledge
//        PlayerClassInfo info = getActiveClass();
//        return info != null ? info.getAbilityInfo(abilityId) : null;
        PlayerAbility ability = MKCoreRegistry.getAbility(abilityId);
        PlayerAbilityInfo info = new PlayerAbilityInfo(ability);
        info.upgrade();
        return info;
    }
}
