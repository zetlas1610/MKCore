package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public class PlayerAbilityInfo {
    private final PlayerAbility ability;
    private int rank;

    public PlayerAbilityInfo(PlayerAbility ability) {
        this.ability = ability;
        rank = GameConstants.ABILITY_INVALID_RANK;
    }

    public PlayerAbility getAbility() {
        return ability;
    }

    public ResourceLocation getId() {
        return ability.getAbilityId();
    }

    public int getRank() {
        return rank;
    }

    public boolean isCurrentlyKnown() {
        return rank > GameConstants.ABILITY_INVALID_RANK;
    }

    public boolean isUpgradeable() {
        return getRank() < getAbility().getMaxRank();
    }

    public boolean upgrade() {
        if (isUpgradeable()) {
            rank += 1;
            return true;
        }
        return false;
    }

    public boolean downgrade() {
        if (isCurrentlyKnown()) {
            rank -= 1;
            return true;
        }
        return false;
    }

    public void serialize(CompoundNBT tag) {
        tag.putString("id", ability.getAbilityId().toString());
        tag.putInt("rank", rank);
    }

    public boolean deserialize(CompoundNBT tag) {
        ResourceLocation id = new ResourceLocation(tag.getString("id"));
        if (!id.equals(ability.getAbilityId())) {
            MKCore.LOGGER.error("Failed to deserialize ability! id was {}, linked ability was {}", id, ability.getAbilityId());
            return false;
        }
        if (tag.contains("rank")) {
            rank = tag.getInt("rank");
        }
        return true;
    }
}
