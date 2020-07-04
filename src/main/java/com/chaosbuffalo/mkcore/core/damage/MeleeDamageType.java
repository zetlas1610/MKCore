package com.chaosbuffalo.mkcore.core.damage;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKCombatFormulas;
import com.chaosbuffalo.mkcore.utils.ItemUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.util.CombatRules;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

public class MeleeDamageType extends MKDamageType {

    public MeleeDamageType(ResourceLocation name) {
        super(name, (RangedAttribute) SharedMonsterAttributes.ATTACK_DAMAGE,
                (RangedAttribute) SharedMonsterAttributes.ARMOR_TOUGHNESS, MKAttributes.MELEE_CRIT,
                MKAttributes.MELEE_CRIT_MULTIPLIER);
    }

    @Override
    public ITextComponent getCritMessage(LivingEntity source, LivingEntity target, float damage,
                                         MKAbility ability, boolean isSelf) {
        Style messageStyle = new Style();
        messageStyle.setColor(TextFormatting.GOLD);
        String msg;
        if (isSelf) {
            msg = String.format("You just crit %s with %s for %s",
                    target.getDisplayName().getFormattedText(),
                    source.getHeldItemMainhand().getDisplayName().getFormattedText(),
                    Math.round(damage));
        } else {
            msg = String.format("%s just crit %s with %s for %s",
                    source.getDisplayName().getFormattedText(),
                    target.getDisplayName().getFormattedText(),
                    source.getHeldItemMainhand().getDisplayName().getFormattedText(),
                    Math.round(damage));
        }
        return new StringTextComponent(msg).setStyle(messageStyle);
    }

    @Override
    public void addAttributes(AbstractAttributeMap attributeMap) {

    }

    @Override
    public float getCritChance(LivingEntity source, LivingEntity target, Entity immediate) {
        ItemStack mainHand = source.getHeldItemMainhand();
        return super.getCritChance(source, target, immediate) + MKCombatFormulas.getCritChanceForItem(mainHand);
    }

    @Override
    public float getCritMultiplier(LivingEntity source, LivingEntity target, Entity immediate) {
        ItemStack mainHand = source.getHeldItemMainhand();
        return super.getCritMultiplier(source, target, immediate) + ItemUtils.getCritMultiplierForItem(mainHand);
    }

    @Override
    public float applyResistance(LivingEntity target, float originalDamage) {
        return CombatRules.getDamageAfterAbsorb(originalDamage, target.getTotalArmorValue(),
                (float) target.getAttribute(getResistanceAttribute()).getValue());
    }
}
