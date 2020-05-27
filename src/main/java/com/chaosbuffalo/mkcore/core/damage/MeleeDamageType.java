package com.chaosbuffalo.mkcore.core.damage;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.CombatRules;
import net.minecraft.util.ResourceLocation;

public class MeleeDamageType extends MKDamageType {

    public MeleeDamageType(ResourceLocation name){
        super(name, null, null);
    }

    @Override
    public float scaleDamage(LivingEntity source, float originalDamage, float modifierScaling) {
        return (float) (originalDamage + source.getAttribute(
                SharedMonsterAttributes.ATTACK_DAMAGE).getValue() * modifierScaling);
    }

    @Override
    public float applyResistance(LivingEntity target, float originalDamage) {
        return originalDamage;
        // if we did want to calculate the real damage absorb of armor, we could use the below
        // but melee damage will have this applied as part of LivingEntity
//        return CombatRules.getDamageAfterAbsorb(originalDamage, target.getTotalArmorValue(),
//                (float) target.getAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getValue());
    }
}
