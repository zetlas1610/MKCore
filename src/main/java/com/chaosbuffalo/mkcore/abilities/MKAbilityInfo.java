package com.chaosbuffalo.mkcore.abilities;

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
    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.putBoolean("known", known);
        return tag;
    }

    @Override
    public boolean deserialize(CompoundNBT tag) {
        if (tag.contains("known")) {
            known = tag.getBoolean("known");
        }
        return true;
    }
}
