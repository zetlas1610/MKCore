package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PlayerDataSyncPacket {

    private final UUID targetUUID;
    private final boolean privateUpdate;
    private final CompoundNBT updateTag;

    public PlayerDataSyncPacket(MKPlayerData player, UUID targetUUID, ISyncObject syncObject, boolean fullSync, boolean privateUpdate) {
        this.targetUUID = targetUUID;
        this.privateUpdate = privateUpdate;
        updateTag = new CompoundNBT();
        if (fullSync) {
            syncObject.serializeFull(updateTag);
        } else {
            syncObject.serializeUpdate(updateTag);
        }
    }


    public PlayerDataSyncPacket(PacketBuffer buffer) {
        targetUUID = buffer.readUniqueId();
        privateUpdate = buffer.readBoolean();
        updateTag = buffer.readCompoundTag();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeUniqueId(targetUUID);
        buffer.writeBoolean(privateUpdate);
        buffer.writeCompoundTag(updateTag);
        MKCore.LOGGER.info("sync toBytes {}", updateTag);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            World world = Minecraft.getInstance().world;
            if (world == null) {
                return;
            }
            PlayerEntity entity = world.getPlayerByUuid(targetUUID);
            if (entity == null)
                return;

            entity.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(cap -> {
                if (cap instanceof MKPlayerData) {
                    ((MKPlayerData) cap).deserializeClientUpdate(updateTag, privateUpdate);
                }
            });
        });
        ctx.setPacketHandled(true);
    }
}
