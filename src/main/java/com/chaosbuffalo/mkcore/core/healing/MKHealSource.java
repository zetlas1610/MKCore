package com.chaosbuffalo.mkcore.core.healing;

import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.init.ModDamageTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class MKHealSource {

    private final Entity immediateSource;
    private final Entity trueSource;
    private final ResourceLocation abilityId;
    private boolean damagesUndead;
    private MKDamageType damageType;
    private float modifierScaling;


    public MKHealSource(ResourceLocation abilityId, Entity source, @Nullable Entity trueSourceIn,
                        MKDamageType damageType, float modifierScaling) {
        this.trueSource = trueSourceIn;
        this.immediateSource = source;
        this.abilityId = abilityId;
        this.damagesUndead = true;
        this.damageType = damageType;
        this.modifierScaling = modifierScaling;
    }

    public static MKHealSource getHolyHeal(ResourceLocation abilityId, Entity source,
                                           @Nullable Entity trueSourceIn, float modifierScaling) {
        return new MKHealSource(abilityId, source, trueSourceIn, ModDamageTypes.HolyDamage, modifierScaling);
    }

    public static MKHealSource getNatureHeal(ResourceLocation abilityId, Entity source,
                                             @Nullable Entity trueSourceIn, float modifierScaling) {
        return new MKHealSource(abilityId, source, trueSourceIn, ModDamageTypes.NatureDamage, modifierScaling);
    }

    public MKDamageType getDamageType() {
        return damageType;
    }

    public float getModifierScaling() {
        return modifierScaling;
    }

    public MKHealSource setModifierScaling(float modifierScaling) {
        this.modifierScaling = modifierScaling;
        return this;
    }

    public MKHealSource setDamageType(MKDamageType damageType) {
        this.damageType = damageType;
        return this;
    }

    public MKHealSource setDamageUndead(boolean damagesUndead) {
        this.damagesUndead = damagesUndead;
        return this;
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
