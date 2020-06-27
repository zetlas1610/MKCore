package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.sync.SyncFloat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;


public class PlayerStatsModule extends PlayerSyncComponent implements IStatsModule {
    private final MKPlayerData playerData;
    private float regenTime;
    private final AbilityTracker abilityTracker;
    private final SyncFloat mana = new SyncFloat("mana", 0f);

    public PlayerStatsModule(MKPlayerData playerData) {
        super("stats");
        this.playerData = playerData;
        regenTime = 0f;
        addPublic(mana);
        abilityTracker = AbilityTracker.getTracker(playerData.getEntity());
        addPrivate(abilityTracker);
    }

    public float getCritChanceForDamageType(MKDamageType damageType) {
        return damageType.getCritChance(getEntity(), null);
    }

    public float getCritMultiplierForDamageType(MKDamageType damageType) {
        return damageType.getCritMultiplier(getEntity(), null);
    }

    @Override
    public float getDamageTypeBonus(MKDamageType damageType) {
        return (float) getEntity().getAttribute(damageType.getDamageAttribute()).getValue();
    }

    public float getDamageMultiplierForDamageType(MKDamageType damageType) {
        float originalValue = 10.0f;
        float scaled = damageType.applyDamage(getEntity(), null, originalValue, 1.0f);
        return scaled / originalValue;
    }


    public float getArmorMultiplierForDamageType(MKDamageType damageType) {
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
        setMana(value, true);
    }

    private void setMana(float value, boolean sendUpdate) {
        value = MathHelper.clamp(value, 0, getMaxMana());
        mana.set(value, sendUpdate);
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
        updateMana();
    }

    private void updateMana() {
        if (getManaRegenRate() <= 0.0f) {
            return;
        }

        float max = getMaxMana();
        if (getMana() > max)
            setMana(max);

        if (getMana() == max)
            return;

        regenTime += 1 / 20f;

        // if getManaRegenRate == 1, this is 1 mana per 3 seconds
        float i_regen = 3.0f / getManaRegenRate();
        while (regenTime >= i_regen) {
            float current = getMana();
            if (current < max) {
//                MKCore.LOGGER.info("regen {} {} {}", regenTime, i_regen, current);
//                MKCore.LOGGER.info("Updating mana {} to {}", current, value);
                float newValue = current + 1;
                setMana(newValue, newValue == max);
            }
            regenTime -= i_regen;
        }
    }

    public void addMana(float value) {
        setMana(getMana() + value);
    }

    public boolean consumeMana(float amount) {
        if (getMana() < amount) {
            return false;
        }

        setMana(getMana() - amount);
        return true;
    }

    @Override
    public int getAbilityCooldown(MKAbility ability) {
        int ticks = ability.getCooldownTicks();
        return MKCombatFormulas.applyCooldownReduction(playerData, ticks);
    }

    public float getAbilityManaCost(MKAbility ability) {
        float manaCost = ability.getManaCost(playerData);
        return MKCombatFormulas.applyManaCostReduction(playerData, manaCost);
    }

    @Override
    public boolean canActivateAbility(MKAbility ability) {
        if (getMana() < getAbilityManaCost(ability))
            return false;
        return true;
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

    public float getTimerPercent(ResourceLocation abilityId, float partialTicks) {
        return abilityTracker.getCooldownPercent(abilityId, partialTicks);
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
    public void resetAllTimers() {
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
}
