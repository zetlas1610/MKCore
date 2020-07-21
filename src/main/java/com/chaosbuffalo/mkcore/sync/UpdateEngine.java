package com.chaosbuffalo.mkcore.sync;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.events.PlayerDataEvent;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerDataSyncPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.MinecraftForge;

public class UpdateEngine {
    private final MKPlayerData playerData;
    private final PlayerEntity player;
    private boolean readyForUpdates = false;
    private final SyncGroup publicUpdater = new SyncGroup();
    private final SyncGroup privateUpdater = new SyncGroup();

    public UpdateEngine(MKPlayerData playerEntity) {
        this.playerData = playerEntity;
        this.player = playerEntity.getEntity();
    }

    public void addPublic(ISyncObject syncObject) {
        publicUpdater.add(syncObject);
        if (syncObject instanceof SyncGroup) {
            ((SyncGroup) syncObject).forceDirty();
        }
    }

    public void removePublic(ISyncObject syncObject) {
        publicUpdater.remove(syncObject);
        if (syncObject instanceof SyncGroup) {
            ((SyncGroup) syncObject).forceDirty();
        }
    }

    public void addPrivate(ISyncObject syncObject) {
        privateUpdater.add(syncObject);
        if (syncObject instanceof SyncGroup) {
            ((SyncGroup) syncObject).forceDirty();
        }
    }

    public void removePrivate(ISyncObject syncObject) {
        privateUpdater.remove(syncObject);
        if (syncObject instanceof SyncGroup) {
            ((SyncGroup) syncObject).forceDirty();
        }
    }

    public void syncUpdates() {
        if (!playerData.isServerSide())
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
        if (!playerData.isServerSide())
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
//        MKCore.LOGGER.info("deserializeClientUpdatePre private:{} {}", privateUpdate, updateTag);
        publicUpdater.deserializeUpdate(updateTag);
        if (privateUpdate) {
            privateUpdater.deserializeUpdate(updateTag);
        }

        MinecraftForge.EVENT_BUS.post(new PlayerDataEvent.Updated(playerData));
    }
}
