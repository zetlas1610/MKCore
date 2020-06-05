package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;


public class MKAbilityInfo implements IMKSerializable<CompoundNBT> {
    private final MKAbility ability;
    private boolean known;

    public MKAbilityInfo(MKAbility ability) {
        this.ability = ability;
        known = false;
    }

    @Nonnull
    public MKAbility getAbility() {
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

    @Override
    public void serialize(CompoundNBT tag) {
        encodeId(this, tag);
        tag.putBoolean("known", known);
    }

    @Override
    public boolean deserialize(CompoundNBT tag) {
        ResourceLocation id = decodeId(tag);
        if (!id.equals(ability.getAbilityId())) {
            MKCore.LOGGER.error("Failed to deserialize ability! id was {}, linked ability was {}", id, ability.getAbilityId());
            return false;
        }
        if (tag.contains("known")) {
            known = tag.getBoolean("known");
        }
        return true;
    }

    public static void encodeId(MKAbilityInfo info, CompoundNBT tag) {
        tag.putString("id", info.getId().toString());
    }

    public static ResourceLocation decodeId(CompoundNBT tag) {
        return new ResourceLocation(tag.getString("id"));
    }
}
