package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerDataSyncPacket;
import com.chaosbuffalo.mkcore.sync.CompositeUpdater;
import com.chaosbuffalo.mkcore.sync.SyncFloat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MKPlayerData implements IMKPlayerData {

    private PlayerEntity player;
    private final SyncFloat mana = new SyncFloat("mana", 0f);
    private final CompositeUpdater dirtyUpdater = new CompositeUpdater(mana);

    public MKPlayerData() {

    }

    @Override
    public void attach(PlayerEntity player) {
        this.player = player;

//        mana.set((float) Math.random());
//        MKCore.LOGGER.info("ctor set mana to {}", mana.get());
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

        if (isDirty()) {
            PlayerDataSyncPacket packet = getUpdateMessage();
            if (packet != null) {
                MKCore.LOGGER.info("sending dirty update for {}", player);
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(this::getPlayer)
                        .send(PacketHandler.getNetworkChannel().toVanillaPacket(packet, NetworkDirection.PLAY_TO_CLIENT));
            }
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
            mana.set(nbt.getFloat("mana"));

            MKCore.LOGGER.info("deserialize({})", mana.get());
        }
    }


    public static class Storage implements Capability.IStorage<IMKPlayerData> {

        @Override
        public CompoundNBT writeNBT(Capability<IMKPlayerData> capability, IMKPlayerData instance, Direction side) {
            CompoundNBT tag = new CompoundNBT();
            instance.serialize(tag);
            return tag;
        }

        @Override
        public void readNBT(Capability<IMKPlayerData> capability, IMKPlayerData instance, Direction side, INBT nbt) {
            if (nbt instanceof CompoundNBT && instance != null) {
                CompoundNBT tag = (CompoundNBT) nbt;
                instance.deserialize(tag);
            }
        }
    }

    public static class Provider implements ICapabilitySerializable<CompoundNBT> {
        private final IMKPlayerData playerHandler;

        public Provider(PlayerEntity playerEntity) {
            this.playerHandler = Capabilities.PLAYER_CAPABILITY.getDefaultInstance();
            if (playerHandler != null) {
                this.playerHandler.attach(playerEntity);
            }
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return Capabilities.PLAYER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> playerHandler));
        }

        @Override
        public CompoundNBT serializeNBT() {
            return (CompoundNBT) Capabilities.PLAYER_CAPABILITY.getStorage().writeNBT(Capabilities.PLAYER_CAPABILITY, playerHandler, null);
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            Capabilities.PLAYER_CAPABILITY.getStorage().readNBT(Capabilities.PLAYER_CAPABILITY, playerHandler, null, nbt);
        }
    }
}
