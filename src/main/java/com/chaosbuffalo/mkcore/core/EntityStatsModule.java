package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public class EntityStatsModule implements IStatsModule {

    private final IMKEntityData entityData;
    private final AbilityTracker abilityTracker;


    public EntityStatsModule(IMKEntityData data){
        entityData = data;
        abilityTracker = AbilityTracker.getTracker(data.getEntity());
    }

    @Override
    public float getDamageTypeBonus(MKDamageType damageType) {
        return (float) getEntity().getAttribute(damageType.getDamageAttribute()).getValue();
    }

    @Override
    public float getHealBonus() {
        return (float) getEntity().getAttribute(MKAttributes.HEAL_BONUS).getValue();
    }

    @Override
    public float getHealth() {
        return getEntity().getHealth();
    }

    @Override
    public void setHealth(float value) {
        getEntity().setHealth(value);
    }

    @Override
    public float getMaxHealth() {
        return getEntity().getMaxHealth();
    }

    @Override
    public int getCurrentAbilityCooldown(ResourceLocation abilityId) {
        return abilityTracker.getCooldownTicks(abilityId);
    }

    @Override
    public int getAbilityCooldown(MKAbility ability) {
        int ticks = ability.getCooldownTicks();
        return MKCombatFormulas.applyCooldownReduction(entityData, ticks);
    }

    @Override
    public float getAbilityManaCost(MKAbility ability) {
        return 0;
    }

    @Override
    public boolean canActivateAbility(MKAbility ability) {
        ResourceLocation abilityId = ability.getAbilityId();
        return getCurrentAbilityCooldown(abilityId) == 0;
    }

    @Override
    public void setTimer(ResourceLocation id, int cooldown) {
        if (cooldown > 0) {
            abilityTracker.setCooldown(id, cooldown);
        } else {
            abilityTracker.removeCooldown(id);
        }
    }

    @Override
    public int getTimer(ResourceLocation id) {
        return abilityTracker.getCooldownTicks(id);
    }

    @Override
    public void resetAllTimers() {
        abilityTracker.removeAll();
    }

    @Override
    public void serialize(CompoundNBT nbt) {

    }

    @Override
    public void deserialize(CompoundNBT nbt) {

    }

    public LivingEntity getEntity(){
        return entityData.getEntity();
    }

}
