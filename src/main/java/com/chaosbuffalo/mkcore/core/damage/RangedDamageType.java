package com.chaosbuffalo.mkcore.core.damage;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.utils.EntityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.util.CombatRules;
import net.minecraft.util.ResourceLocation;

public class RangedDamageType extends MKDamageType {
    public RangedDamageType(ResourceLocation name) {
        super(name, MKAttributes.RANGED_DAMAGE, MKAttributes.RANGED_RESISTANCE, MKAttributes.RANGED_CRIT, MKAttributes.RANGED_CRIT_MULTIPLIER);
    }

    @Override
    public void addAttributes(AbstractAttributeMap attributeMap) {
        super.addAttributes(attributeMap);
        attributeMap.registerAttribute(getCritChanceAttribute());
        attributeMap.registerAttribute(getCritMultiplierAttribute());
    }

    @Override
    public float getCritChance(LivingEntity source, LivingEntity target, Entity immediate) {
        float chance = super.getCritChance(source, target, immediate);
        return chance + EntityUtils.ENTITY_CRIT.getChance(immediate);
    }

    @Override
    public float getCritMultiplier(LivingEntity source, LivingEntity livingTarget, Entity immediate) {
        float damageMultiplier = super.getCritMultiplier(source, livingTarget, immediate);
        damageMultiplier += EntityUtils.ENTITY_CRIT.getMultiplier(immediate);
        if (livingTarget.isGlowing()) {
            damageMultiplier += 1.0f;
        }
        return damageMultiplier;
    }

    @Override
    public float applyResistance(LivingEntity target, float originalDamage) {
        return CombatRules.getDamageAfterAbsorb(originalDamage, target.getTotalArmorValue(),
                (float) target.getAttribute(getResistanceAttribute()).getValue());
    }
}
