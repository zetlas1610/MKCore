package com.chaosbuffalo.mkcore.core.damage;

import com.chaosbuffalo.mkcore.init.ModDamageTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class MKDamageSource extends IndirectEntityDamageSource {
    private final ResourceLocation abilityId;
    private float modifierScaling;
    private boolean suppressTriggers;
    private final DamageType damageType;

    public ResourceLocation getAbilityId() {
        return abilityId;
    }

    public MKDamageSource(ResourceLocation abilityId, DamageType damageTypeIn,
                          Entity source, @Nullable Entity indirectEntityIn) {
        super(damageTypeIn.getRegistryName().toString(), source, indirectEntityIn);
        this.abilityId = abilityId;
        this.modifierScaling = 1.0f;
        this.damageType = damageTypeIn;
    }

    public float getModifierScaling() {
        return modifierScaling;
    }

    public MKDamageSource setModifierScaling(float value) {
        modifierScaling = value;
        return this;
    }

    public DamageType getMKDamageType(){
        return damageType;
    }

    public boolean isMeleeDamage(){
        return damageType.equals(ModDamageTypes.MeleeDamage);
    }

    public boolean shouldSuppressTriggers() {
        return suppressTriggers;
    }

    public MKDamageSource setSuppressTriggers(boolean suppressTriggers) {
        this.suppressTriggers = suppressTriggers;
        return this;
    }

    public static MKDamageSource causeAbilityDamage(DamageType damageType, ResourceLocation abilityId, Entity source,
                                                    @Nullable Entity indirectEntityIn){
        if (damageType.equals(ModDamageTypes.MeleeDamage)){
            return causeMeleeDamage(abilityId, source, indirectEntityIn);
        }
        return (MKDamageSource) new MKDamageSource(abilityId, damageType, source, indirectEntityIn)
                .setDamageBypassesArmor();
    }

    public static MKDamageSource causeAbilityDamage(DamageType damageType, ResourceLocation abilityId, Entity source,
                                                    @Nullable Entity indirectEntityIn, float modifierScaling){
        return causeAbilityDamage(damageType, abilityId, source, indirectEntityIn)
                .setModifierScaling(modifierScaling);
    }


    public static MKDamageSource causeMeleeDamage(ResourceLocation abilityId, Entity source,
                                                  @Nullable Entity indirectEntityIn) {
        return new MKDamageSource(abilityId, ModDamageTypes.MeleeDamage, source, indirectEntityIn);
    }

    public static MKDamageSource causeMeleeDamage(ResourceLocation abilityId, Entity source,
                                                  @Nullable Entity indirectEntityIn, float modifierScaling) {
        return causeMeleeDamage(abilityId, source, indirectEntityIn).setModifierScaling(modifierScaling);
    }
}
