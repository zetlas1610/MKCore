package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.sync.CompositeUpdater;
import com.chaosbuffalo.mkcore.sync.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import com.chaosbuffalo.mkcore.sync.SyncFloat;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class PlayerStatsModule implements ISyncObject, IStatsModule {
    private final MKPlayerData playerData;
    private float regenTime;
    private final AbilityTracker abilityTracker;
    private final SyncFloat mana = new SyncFloat("mana", 0f);
    private final CompositeUpdater publicUpdater = new CompositeUpdater(mana);

    public PlayerStatsModule(MKPlayerData playerData) {
        this.playerData = playerData;
        regenTime = 0f;
        abilityTracker = AbilityTracker.getTracker(playerData.getEntity());
        playerData.getUpdateEngine().addPrivate(abilityTracker);
    }

    public float getCritChanceForDamageType(MKDamageType damageType){
        return damageType.getCritChance(getEntity(), null);
    }

    public float getCritMultiplierForDamageType(MKDamageType damageType){
        return damageType.getCritMultiplier(getEntity(), null);
    }

    @Override
    public float getDamageTypeBonus(MKDamageType damageType){
        return (float) getEntity().getAttribute(damageType.getDamageAttribute()).getValue();
    }

    public float getDamageMultiplierForDamageType(MKDamageType damageType){
        float originalValue = 10.0f;
        float scaled = damageType.applyDamage(getEntity(), null, originalValue, 1.0f);
        return scaled / originalValue;
    }


    public float getArmorMultiplierForDamageType(MKDamageType damageType){
        float originalValue = 10.0f;
        float scaled = damageType.applyResistance(getEntity(), originalValue);
        return scaled / originalValue;
    }

    public float getMeleeCritChance() {
        return (float) getEntity().getAttribute(MKAttributes.MELEE_CRIT).getValue();
    }

    public float getSpellCritChance() {
        return (float) getEntity().getAttribute(MKAttributes.SPELL_CRIT).getValue();
    }

    public float getSpellCritDamage() {
        return (float) getEntity().getAttribute(MKAttributes.SPELL_CRIT_MULTIPLIER).getValue();
    }

    public float getMeleeCritDamage() {
        return (float) getEntity().getAttribute(MKAttributes.MELEE_CRIT_MULTIPLIER).getValue();
    }

    @Override
    public float getHealBonus() {
        return (float) getEntity().getAttribute(MKAttributes.HEAL_BONUS).getValue();
    }


    private boolean isServerSide() {
        return getEntity() instanceof ServerPlayerEntity;
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

    public float getMana() {
        return mana.get();
    }

    public void setMana(float value) {
        value = MathHelper.clamp(value, 0, getMaxMana());
        mana.set(value);
    }

    public float getMaxMana() {
        return (float) getEntity().getAttribute(MKAttributes.MAX_MANA).getValue();
    }

    public void setMaxMana(float max) {
        getEntity().getAttribute(MKAttributes.MAX_MANA).setBaseValue(max);
        setMana(getMana()); // Refresh the mana to account for the updated maximum
    }

    public float getManaRegenRate() {
        return (float) getEntity().getAttribute(MKAttributes.MANA_REGEN).getValue();
    }

    public void tick() {
        abilityTracker.tick();

        if (isServerSide()) {
            updateMana();
        }
    }

    private void updateMana() {
        if (this.getManaRegenRate() <= 0.0f) {
            return;
        }

        float max = getMaxMana();
        if (getMana() > max)
            setMana(max);

        regenTime += 1. / 20.;
        // if getManaRegenRate == 1, this is 1 mana per 3 seconds
        float i_regen = 3.0f / getManaRegenRate();
        if (regenTime >= i_regen) {
            if (getMana() < max) {
                addMana(1);
            }
            regenTime -= i_regen;
        }
    }

    public void addMana(float value) {
        setMana(getMana() + value);
    }

    @Override
    public boolean consumeMana(float amount) {
        if (getMana() >= amount) {
            setMana(getMana() - amount);
            return true;
        }
        return false;
    }

    @Override
    public int getCurrentAbilityCooldown(ResourceLocation abilityId) {
        MKAbilityInfo abilityInfo = playerData.getKnowledge().getAbilityInfo(abilityId);
        return abilityInfo != null ? abilityTracker.getCooldownTicks(abilityId) : GameConstants.ACTION_BAR_INVALID_COOLDOWN;
    }

    @Override
    public float getActiveCooldownPercent(MKAbilityInfo abilityInfo, float partialTicks) {
        return abilityInfo != null ? abilityTracker.getCooldown(abilityInfo.getId(), partialTicks) : 0.0f;
    }

    @Override
    public int getAbilityCooldown(MKAbility ability) {
        int ticks = ability.getCooldownTicks();
        ticks = MKCombatFormulas.applyCooldownReduction(playerData, ticks);
        return ticks;
    }

    public float getAbilityManaCost(ResourceLocation abilityId) {
        MKAbilityInfo abilityInfo = playerData.getKnowledge().getAbilityInfo(abilityId);
        if (abilityInfo == null) {
            return 0.0f;
        }
        float manaCost = abilityInfo.getAbility().getManaCost();
        return MKCombatFormulas.applyManaCostReduction(playerData, manaCost);
    }

    @Override
    public boolean canActivateAbility(MKAbility ability) {
        ResourceLocation abilityId = ability.getAbilityId();
        return getMana() >= getAbilityManaCost(abilityId) &&
                getCurrentAbilityCooldown(abilityId) == 0;
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

    public void printActiveCooldowns() {
        String msg = "All active cooldowns:";

        getEntity().sendMessage(new StringTextComponent(msg));
        abilityTracker.iterateActive((abilityId, current) -> {
            String name = abilityId.toString();
            int max = abilityTracker.getMaxCooldownTicks(abilityId);
            ITextComponent line = new StringTextComponent(String.format("%s: %d / %d", name, current, max));
            getEntity().sendMessage(line);
        });
    }

    @Override
    public void resetAllCooldowns() {
        abilityTracker.removeAll();
    }

    private PlayerEntity getEntity() {
        return playerData.getEntity();
    }


    @Override
    public void serialize(CompoundNBT nbt) {
        abilityTracker.serialize(nbt);
        nbt.putFloat("mana", mana.get());
    }

    @Override
    public void deserialize(CompoundNBT nbt) {
        abilityTracker.deserialize(nbt);
        // TODO: activate persona here
        if (nbt.contains("mana")) {
            setMana(nbt.getFloat("mana"));
        }
    }


    @Override
    public void setNotifier(ISyncNotifier notifier) {
        publicUpdater.setNotifier(notifier);
    }

    @Override
    public boolean isDirty() {
        return publicUpdater.isDirty();
    }

    @Override
    public void deserializeUpdate(CompoundNBT tag) {
        publicUpdater.deserializeUpdate(tag);
    }

    @Override
    public void serializeUpdate(CompoundNBT tag) {
        publicUpdater.serializeUpdate(tag);
    }

    @Override
    public void serializeFull(CompoundNBT tag) {
        publicUpdater.serializeFull(tag);
    }


}
