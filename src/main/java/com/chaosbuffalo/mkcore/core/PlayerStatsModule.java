package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.PlayerAbility;
import com.chaosbuffalo.mkcore.abilities.PlayerAbilityInfo;
import com.chaosbuffalo.mkcore.sync.CompositeUpdater;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import com.chaosbuffalo.mkcore.sync.SyncFloat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class PlayerStatsModule implements ISyncObject {
    private final MKPlayerData playerData;
    private float regenTime;
    private final AbilityTracker abilityTracker;
    private final SyncFloat mana = new SyncFloat("mana", 0f);
    private final CompositeUpdater publicUpdater = new CompositeUpdater(mana);

    public PlayerStatsModule(MKPlayerData playerData) {
        this.playerData = playerData;
        regenTime = 0f;
        abilityTracker = AbilityTracker.getTracker(playerData.getPlayer());
        playerData.getUpdateEngine().addPrivate(abilityTracker);
    }

    public float getMeleeCritChance() {
        return (float) getPlayer().getAttribute(MKAttributes.MELEE_CRIT).getValue();
    }

    public float getSpellCritChance() {
        return (float) getPlayer().getAttribute(MKAttributes.SPELL_CRIT).getValue();
    }

    public float getMagicArmor() {
        return (float) getPlayer().getAttribute(MKAttributes.ARCANE_RESISTANCE).getValue();
    }

    public float getSpellCritDamage() {
        return (float) getPlayer().getAttribute(MKAttributes.SPELL_CRITICAL_DAMAGE).getValue();
    }

    public float getMeleeCritDamage() {
        return (float) getPlayer().getAttribute(MKAttributes.MELEE_CRITICAL_DAMAGE).getValue();
    }

    public float getHealBonus() {
        return (float) getPlayer().getAttribute(MKAttributes.HEAL_BONUS).getValue();
    }

    public float getMagicDamageBonus() {
        return (float) getPlayer().getAttribute(MKAttributes.ARCANE_DAMAGE).getValue();
    }

    private PlayerEntity getPlayer() {
        return playerData.getPlayer();
    }

    private boolean isServerSide() {
        return getPlayer() instanceof ServerPlayerEntity;
    }

    public float getHealth() {
        return getPlayer().getHealth();
    }

    public void setHealth(float value) {
        getPlayer().setHealth(value);
    }

    public float getMaxHealth() {
        return getPlayer().getMaxHealth();
    }

    public float getMana() {
        return mana.get();
    }

    public void setMana(float value) {
        value = MathHelper.clamp(value, 0, getMaxMana());
        mana.set(value);
    }

    public float getMaxMana() {
        return (float) getPlayer().getAttribute(MKAttributes.MAX_MANA).getValue();
    }

    public void setMaxMana(float max) {
        getPlayer().getAttribute(MKAttributes.MAX_MANA).setBaseValue(max);
        setMana(getMana()); // Refresh the mana to account for the updated maximum
    }

    public float getManaRegenRate() {
        return (float) getPlayer().getAttribute(MKAttributes.MANA_REGEN).getValue();
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

    public boolean consumeMana(float amount) {
        if (getMana() >= amount) {
            setMana(getMana() - amount);
            return true;
        }
        return false;
    }

    public int getCurrentAbilityCooldown(ResourceLocation abilityId) {
        PlayerAbilityInfo abilityInfo = playerData.getKnowledge().getAbilityInfo(abilityId);
        return abilityInfo != null ? abilityTracker.getCooldownTicks(abilityId) : GameConstants.ACTION_BAR_INVALID_COOLDOWN;
    }

    public float getActiveCooldownPercent(PlayerAbilityInfo abilityInfo, float partialTicks) {
        return abilityInfo != null ? abilityTracker.getCooldown(abilityInfo.getId(), partialTicks) : 0.0f;
    }

    public int getAbilityCooldown(PlayerAbility ability) {
        int ticks = ability.getCooldownTicks();
        ticks = PlayerFormulas.applyCooldownReduction(playerData, ticks);
        return ticks;
    }

    public float getAbilityManaCost(ResourceLocation abilityId) {
        PlayerAbilityInfo abilityInfo = playerData.getKnowledge().getAbilityInfo(abilityId);
        if (abilityInfo == null) {
            return 0.0f;
        }
        float manaCost = abilityInfo.getAbility().getManaCost();
        return PlayerFormulas.applyManaCostReduction(playerData, manaCost);
    }

    public boolean canActivateAbility(PlayerAbility ability) {
        ResourceLocation abilityId = ability.getAbilityId();
        return getMana() >= getAbilityManaCost(abilityId) &&
                getCurrentAbilityCooldown(abilityId) == 0;
    }

    public void setTimer(ResourceLocation id, int cooldown) {
        if (cooldown > 0) {
            abilityTracker.setCooldown(id, cooldown);
        } else {
            abilityTracker.removeCooldown(id);
        }
    }

    public int getTimer(ResourceLocation id) {
        return abilityTracker.getCooldownTicks(id);
    }

    public void printActiveCooldowns() {
        String msg = "All active cooldowns:";

        getPlayer().sendMessage(new StringTextComponent(msg));
        abilityTracker.iterateActive((abilityId, current) -> {
            String name = abilityId.toString();
            int max = abilityTracker.getMaxCooldownTicks(abilityId);
            ITextComponent line = new StringTextComponent(String.format("%s: %d / %d", name, current, max));
            getPlayer().sendMessage(line);
        });
    }

    public void resetAllCooldowns() {
        abilityTracker.removeAll();
    }


    public void serialize(CompoundNBT nbt) {
        abilityTracker.serialize(nbt);
        nbt.putFloat("mana", mana.get());
    }

    public void deserialize(CompoundNBT nbt) {
        abilityTracker.deserialize(nbt);
        // TODO: activate persona here
        if (nbt.contains("mana")) {
            setMana(nbt.getFloat("mana"));
        }
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
