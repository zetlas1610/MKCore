package com.chaosbuffalo.mkcore.sync;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerDataSyncPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public class UpdateEngine {
    private final MKPlayerData playerData;
    private final PlayerEntity player;
    private boolean readyForUpdates = false;
    private final CompositeUpdater publicUpdater = new CompositeUpdater();
    private final CompositeUpdater privateUpdater = new CompositeUpdater();

    public UpdateEngine(MKPlayerData playerEntity) {
        this.playerData = playerEntity;
        this.player = playerEntity.getEntity();
    }

    private boolean isServerSide() {
        return player instanceof ServerPlayerEntity;
    }

    public void addPublic(ISyncObject syncObject) {
        publicUpdater.add(syncObject);
    }

    public void addPrivate(ISyncObject syncObject) {
        privateUpdater.add(syncObject);
    }

    public void syncUpdates() {
        if (!isServerSide())
            return;

        if (!readyForUpdates) {
//            MKCore.LOGGER.info("deferring update because client not ready");
            return;
        }

        if (publicUpdater.isDirty()) {
            PlayerDataSyncPacket packet = getUpdateMessage(false);
            MKCore.LOGGER.info("sending public dirty update {} for {}", packet, player);
            PacketHandler.sendToTrackingAndSelf(packet, (ServerPlayerEntity) player);
        }

        if (privateUpdater.isDirty()) {
            PlayerDataSyncPacket packet = getUpdateMessage(true);
            MKCore.LOGGER.info("sending private dirty update {} for {}", packet, player);
            PacketHandler.sendMessage(packet, (ServerPlayerEntity) player);
        }
    }

    private PlayerDataSyncPacket getUpdateMessage(boolean privateUpdate) {
        CompoundNBT tag = new CompoundNBT();
        serializeUpdate(tag, false, privateUpdate);
        return new PlayerDataSyncPacket(player.getUniqueID(), tag, privateUpdate);
    }

    public void sendAll(ServerPlayerEntity otherPlayer) {
        if (!isServerSide())
            return;
        CompoundNBT tag = new CompoundNBT();
        publicUpdater.serializeFull(tag);
        boolean privateUpdate = player == otherPlayer;
        if (privateUpdate) {
            privateUpdater.serializeFull(tag);
            readyForUpdates = true;
        }
        PlayerDataSyncPacket packet = new PlayerDataSyncPacket(player.getUniqueID(), tag, privateUpdate);
        MKCore.LOGGER.info("sending full sync {} for {} to {}", packet, player, otherPlayer);
        PacketHandler.sendMessage(packet, otherPlayer);
    }

    public void serializeUpdate(CompoundNBT updateTag, boolean fullSync, boolean privateUpdate) {
//        MKCore.LOGGER.info("serializeClientUpdate full:{} private:{}", fullSync, privateUpdate);
        ISyncObject updater = privateUpdate ? privateUpdater : publicUpdater;
        if (fullSync) {
            updater.serializeFull(updateTag);
        } else {
            updater.serializeUpdate(updateTag);
        }
    }

    public void deserializeUpdate(CompoundNBT updateTag, boolean privateUpdate) {
//        MKCore.LOGGER.info("deserializeClientUpdatePre private:{}", privateUpdate);
        if (privateUpdate) {
            privateUpdater.deserializeUpdate(updateTag);
        } else {
            publicUpdater.deserializeUpdate(updateTag);
        }
    }
}
