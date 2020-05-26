package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.sync.CompositeUpdater;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import com.chaosbuffalo.mkcore.sync.SyncFloat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;

public class PlayerStatsModule implements ISyncObject {
    private final MKPlayerData playerData;
    private float regenTime;
    private final SyncFloat mana = new SyncFloat("mana", 0f);
    private final CompositeUpdater publicUpdater = new CompositeUpdater(mana);

    public PlayerStatsModule(MKPlayerData playerData) {
        this.playerData = playerData;
        regenTime = 0f;
    }

    private PlayerEntity getPlayer() {
        return playerData.getPlayer();
    }

    private boolean isServerSide() {
        return getPlayer() instanceof ServerPlayerEntity;
    }

    public float getMana() {
        return mana.get();
    }

    public void setMana(float value) {
        value = MathHelper.clamp(value, 0, getMaxMana());
        mana.set(value);
    }

    public float getMaxMana() {
        return (float) getPlayer().getAttribute(PlayerAttributes.MAX_MANA).getValue();
    }

    public void setMaxMana(float max) {
        getPlayer().getAttribute(PlayerAttributes.MAX_MANA).setBaseValue(max);
        setMana(getMana()); // Refresh the mana to account for the updated maximum
    }

    public float getManaRegenRate() {
        return (float) getPlayer().getAttribute(PlayerAttributes.MANA_REGEN).getValue();
    }

    public void tick() {

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

    public void serialize(CompoundNBT nbt) {
        nbt.putFloat("mana", mana.get());
    }

    public void deserialize(CompoundNBT nbt) {
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
