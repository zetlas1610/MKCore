package com.chaosbuffalo.mkcore.core.healing;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class MKHealSource {

    private final Entity immediateSource;
    private final Entity trueSource;
    private final ResourceLocation abilityId;
    private boolean damagesUndead;


    public MKHealSource(ResourceLocation abilityId, Entity source, @Nullable Entity trueSourceIn){
        this.trueSource = trueSourceIn;
        this.immediateSource = source;
        this.abilityId = abilityId;
        this.damagesUndead = true;
    }

    public void setDamageUndead(boolean damagesUndead) {
        this.damagesUndead = damagesUndead;
    }

    public boolean doesDamageUndead() {
        return damagesUndead;
    }

    @Nullable
    public Entity getImmediateSource() {
        return immediateSource;
    }

    @Nullable
    public Entity getTrueSource() {
        return trueSource;
    }

    public ResourceLocation getAbilityId() {
        return abilityId;
    }
}
