package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerDataSyncPacket;
import com.chaosbuffalo.mkcore.sync.CompositeUpdater;
import com.chaosbuffalo.mkcore.sync.SyncFloat;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.PacketDistributor;

public class MKPlayerData implements IMKPlayerData {

    private PlayerEntity player;
    private float regenTime;
    private final SyncFloat mana = new SyncFloat("mana", 0f);
    private final CompositeUpdater dirtyUpdater = new CompositeUpdater(mana);

    public MKPlayerData() {
        regenTime = 0f;
    }

    @Override
    public void attach(PlayerEntity player) {
        this.player = player;
        registerAttributes();

        AttributeModifier mod = new AttributeModifier("test max mana", 20, AttributeModifier.Operation.ADDITION).setSaved(false);
        player.getAttribute(PlayerAttributes.MAX_MANA).applyModifier(mod);

        AttributeModifier mod2 = new AttributeModifier("test mana regen", 1, AttributeModifier.Operation.ADDITION).setSaved(false);
        player.getAttribute(PlayerAttributes.MANA_REGEN).applyModifier(mod2);
    }

    private void registerAttributes() {
        AbstractAttributeMap attributes = player.getAttributes();
        attributes.registerAttribute(PlayerAttributes.MAX_MANA);
        attributes.registerAttribute(PlayerAttributes.MANA_REGEN);
    }

    @Override
    public float getMana() {
        return mana.get();
    }

    @Override
    public void setMana(float value) {
        value = MathHelper.clamp(value, 0, getMaxMana());
        mana.set(value);
    }

    @Override
    public void setMaxMana(float max) {
        player.getAttribute(PlayerAttributes.MAX_MANA).setBaseValue(max);
        setMana(getMana()); // Refresh the mana to account for the updated maximum
    }

    @Override
    public void clone(IMKPlayerData previous) {
        MKCore.LOGGER.info("onDeath!");

        CompoundNBT tag = new CompoundNBT();
        previous.serialize(tag);
        deserialize(tag);
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    private boolean isServerSide() {
        return player instanceof ServerPlayerEntity;
    }

    @Override
    public void update() {

//        MKCore.LOGGER.info("update {} {}", this.player, mana.get());

        if (!isServerSide()) {
            // client-only handling here
            return;
        }

        updateMana();

        if (isDirty()) {
            PlayerDataSyncPacket packet = getUpdateMessage();
            if (packet != null) {
                MKCore.LOGGER.info("sending dirty update for {}", player);
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(this::getPlayer)
                        .send(PacketHandler.getNetworkChannel().toVanillaPacket(packet, NetworkDirection.PLAY_TO_CLIENT));
            }
        }
    }

    private void updateMana() {
        if (this.getManaRegenRate() <= 0.0f) {
            return;
        }

        if (getMana() > getMaxMana())
            setMana(getMaxMana());

        regenTime += 1. / 20.;
        float i_regen = 3.0f / this.getManaRegenRate();
        if (regenTime >= i_regen) {
            MKCore.LOGGER.info("regen - adding 1 mana");
            if (this.getMana() < this.getMaxMana()) {
                addMana(1);
            }
            regenTime -= i_regen;
        }
    }

    private boolean isDirty() {
        return dirtyUpdater.isDirty();
    }

    private PlayerDataSyncPacket getUpdateMessage() {
        return isDirty() ? new PlayerDataSyncPacket(this, player.getUniqueID()) : null;
    }


    public void serializeClientUpdate(CompoundNBT updateTag) {
        MKCore.LOGGER.info("serializeClientUpdate {}", mana.get());
        dirtyUpdater.serializeUpdate(updateTag);
    }

    public void deserializeClientUpdate(CompoundNBT updateTag) {
        MKCore.LOGGER.info("deserializeClientUpdatePre {}", mana.get());
        dirtyUpdater.deserializeUpdate(updateTag);
        MKCore.LOGGER.info("deserializeClientUpdatePost - {}", mana.get());
    }


    @Override
    public void serialize(CompoundNBT nbt) {
        nbt.putFloat("mana", mana.get());
    }

    @Override
    public void deserialize(CompoundNBT nbt) {
        if (nbt.contains("mana")) {
            setMana(nbt.getFloat("mana"));
        }
        MKCore.LOGGER.info("deserialize({})", mana.get());
    }
}
