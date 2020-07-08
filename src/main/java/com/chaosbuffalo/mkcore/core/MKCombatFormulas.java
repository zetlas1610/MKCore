package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.utils.EntityUtils;
import com.chaosbuffalo.mkcore.utils.ItemUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;

public class MKCombatFormulas {

    public static int applyCooldownReduction(IMKEntityData entityData, int originalCooldownTicks) {
        final float MAX_COOLDOWN = 2.0f; // Maximum cooldown rate improvement is 200%
        float cdrValue = (float) entityData.getEntity().getAttribute(MKAttributes.COOLDOWN).getValue();
        float mod = MAX_COOLDOWN - cdrValue;
        float newTicks = mod * originalCooldownTicks;
        return (int) newTicks;
    }

    public static int applyCastTimeModifier(IMKEntityData entityData, int originalCastTicks) {
        final float MAX_RATE = 2.0f; // Maximum rate improvement is 200%
        float castSpeed = (float) entityData.getEntity().getAttribute(MKAttributes.CASTING_SPEED).getValue();
        float mod = MAX_RATE - castSpeed;
        float newTicks = mod * originalCastTicks;
        return (int) newTicks;
    }

    public static float applyManaCostReduction(IMKEntityData playerData, float originalCost) {
        return originalCost;
    }

    public static float applyHealBonus(IMKEntityData entityData, float amount, float modifierScaling) {
        float mod = entityData.getStats().getHealBonus();
        return amount + mod * modifierScaling;
    }

    public static float getCritChanceForItem(ItemStack item) {
        return ItemUtils.getCritChanceForItem(item);
    }

    public static float getRangedCritChanceForEntity(IMKEntityData data, ServerPlayerEntity player, Entity entity) {
        return EntityUtils.ENTITY_CRIT.getChance(entity);
    }

    public static boolean checkCrit(LivingEntity entity, float chance) {
        return entity.getRNG().nextFloat() >= 1.0f - chance;
    }

//    public static int applyBuffDurationBonus(IPlayerData data, int duration) {
//        return (int) (duration * data.getBuffDurationBonus());
//    }
}
