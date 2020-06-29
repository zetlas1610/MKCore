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


    public MKHealSource(ResourceLocation abilityId, Entity source, @Nullable Entity trueSourceIn,
                        MKDamageType damageType){
        this.trueSource = trueSourceIn;
        this.immediateSource = source;
        this.abilityId = abilityId;
        this.damagesUndead = true;
        this.damageType = damageType;
    }

    public static MKHealSource getHolyHeal(ResourceLocation abilityId, Entity source,
                                           @Nullable Entity trueSourceIn){
        return new MKHealSource(abilityId, source, trueSourceIn, ModDamageTypes.HolyDamage);
    }

    public static MKHealSource getNatureHeal(ResourceLocation abilityId, Entity source,
                                             @Nullable Entity trueSourceIn){
        return new MKHealSource(abilityId, source, trueSourceIn, ModDamageTypes.NatureDamage);
    }

    public MKDamageType getDamageType() {
        return damageType;
    }

    public void setDamageType(MKDamageType damageType) {
        this.damageType = damageType;
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
