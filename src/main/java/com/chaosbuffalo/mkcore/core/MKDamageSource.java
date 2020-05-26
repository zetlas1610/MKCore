package com.chaosbuffalo.mkcore.core;

import net.minecraft.entity.Entity;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class MKDamageSource extends IndirectEntityDamageSource {

    private static String ABILITY_DMG_TYPE = "mk_ability_damage";
    private static String MELEE_ABILITY_DMG_TYPE = "mk_melee_ability_damage";
    private static String INDIRECT_MAGIC_DMG_TYPE = "mk_indirect_magic_damage";
    private static String HOLY_DMG_TYPE = "mk_holy_ability_damage";

    private final ResourceLocation abilityId;
    private float modifierScaling;
    private boolean suppressTriggers;

    public ResourceLocation getAbilityId() {
        return abilityId;
    }

    public MKDamageSource(ResourceLocation abilityId, String damageTypeIn,
                          Entity source, @Nullable Entity indirectEntityIn) {
        super(damageTypeIn, source, indirectEntityIn);
        this.abilityId = abilityId;
        this.modifierScaling = 1.0f;
    }

    public float getModifierScaling() {
        return modifierScaling;
    }

    public MKDamageSource setModifierScaling(float value) {
        modifierScaling = value;
        return this;
    }

    public boolean shouldSuppressTriggers() {
        return suppressTriggers;
    }

    public MKDamageSource setSuppressTriggers(boolean suppressTriggers) {
        this.suppressTriggers = suppressTriggers;
        return this;
    }

    public boolean isIndirectMagic() {
        return damageType.equals(INDIRECT_MAGIC_DMG_TYPE);
    }

    public boolean isHolyDamage() {
        return damageType.equals(HOLY_DMG_TYPE);
    }

    public boolean isMeleeAbility() {
        return damageType.equals(MELEE_ABILITY_DMG_TYPE);
    }

    public static MKDamageSource causeIndirectMagicDamage(ResourceLocation abilityId, Entity source,
                                                          @Nullable Entity indirectEntityIn) {
        String damageType = ABILITY_DMG_TYPE;
        return (MKDamageSource) new MKDamageSource(abilityId, damageType, source, indirectEntityIn)
                .setDamageBypassesArmor()
                .setMagicDamage();
    }

    public static MKDamageSource causeIndirectMagicDamage(ResourceLocation abilityId, Entity source,
                                                          @Nullable Entity indirectEntityIn, float modifierScaling) {
        return causeIndirectMagicDamage(abilityId, source, indirectEntityIn).setModifierScaling(modifierScaling);
    }


    public static MKDamageSource fromMeleeSkill(ResourceLocation abilityId, Entity source,
                                                @Nullable Entity indirectEntityIn) {
        return new MKDamageSource(abilityId, MELEE_ABILITY_DMG_TYPE, source, indirectEntityIn);
    }

    public static MKDamageSource fromMeleeSkill(ResourceLocation abilityId, Entity source,
                                                @Nullable Entity indirectEntityIn, float modifierScaling) {
        return fromMeleeSkill(abilityId, source, indirectEntityIn).setModifierScaling(modifierScaling);
    }
}
