package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.sync.SyncFloat;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;


public class PlayerStatsModule extends EntityStatsModule implements IStatsModule, IPlayerSyncComponentProvider {
    private final PlayerSyncComponent sync = new PlayerSyncComponent("stats");
    private float manaRegenTimer;
    private final SyncFloat mana = new SyncFloat("mana", 0f);

    public PlayerStatsModule(MKPlayerData playerData) {
        super(playerData);
        manaRegenTimer = 0f;
        addSyncPublic(mana);
        addSyncPrivate(abilityTracker);
    }

    @Override
    public PlayerSyncComponent getSyncComponent() {
        return sync;
    }

    public float getCritChanceForDamageType(MKDamageType damageType) {
        return damageType.getCritChance(getEntity(), null);
    }

    public float getCritMultiplierForDamageType(MKDamageType damageType) {
        return damageType.getCritMultiplier(getEntity(), null);
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
        super.tick();
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

        manaRegenTimer += 1 / 20f;

        // if getManaRegenRate == 1, this is 1 mana per 3 seconds
        float i_regen = 3.0f / getManaRegenRate();
        while (manaRegenTimer >= i_regen) {
            float current = getMana();
            if (current < max) {
//                MKCore.LOGGER.info("regen {} {} {}", regenTime, i_regen, current);
//                MKCore.LOGGER.info("Updating mana {} to {}", current, value);
                float newValue = current + 1;
                setMana(newValue, newValue == max);
            }
            manaRegenTimer -= i_regen;
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

    public float getAbilityManaCost(MKAbility ability) {
        float manaCost = ability.getManaCost(entityData);
        return MKCombatFormulas.applyManaCostReduction(entityData, manaCost);
    }

    @Override
    public boolean canActivateAbility(MKAbility ability) {
        if (getMana() < getAbilityManaCost(ability))
            return false;
        return true;
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

    public void refreshStats() {
        if (getHealth() > getMaxHealth()) {
            setHealth(MathHelper.clamp(getHealth(), 0, getMaxHealth()));
        }
        if (getMana() > getMaxMana()) {
            setMana(getMana());
        }
    }

    public void onPersonaActivated() {
        refreshStats();
    }

    public void onPersonaDeactivated() {

    }

    @Override
    public void serialize(CompoundNBT nbt) {
        abilityTracker.serialize(nbt);
        nbt.putFloat("mana", mana.get());
    }

    @Override
    public void deserialize(CompoundNBT nbt) {
        abilityTracker.deserialize(nbt);
        if (nbt.contains("mana")) {
            setMana(nbt.getFloat("mana"));
        }
    }
}
