package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public class PlayerAbilityInfo {
    private final PlayerAbility ability;
    private boolean known;

    public PlayerAbilityInfo(PlayerAbility ability) {
        this.ability = ability;
        known = false;
    }

    public PlayerAbility getAbility() {
        return ability;
    }

    public ResourceLocation getId() {
        return ability.getAbilityId();
    }

    public boolean isCurrentlyKnown() {
        return known;
    }

    public void setKnown(boolean learn) {
        this.known = learn;
    }

    public void serialize(CompoundNBT tag) {
        tag.putString("id", ability.getAbilityId().toString());
        tag.putBoolean("known", known);
    }

    public boolean deserialize(CompoundNBT tag) {
        ResourceLocation id = new ResourceLocation(tag.getString("id"));
        if (!id.equals(ability.getAbilityId())) {
            MKCore.LOGGER.error("Failed to deserialize ability! id was {}, linked ability was {}", id, ability.getAbilityId());
            return false;
        }
        if (tag.contains("known")) {
            known = tag.getBoolean("known");
        }
        return true;
    }
}
