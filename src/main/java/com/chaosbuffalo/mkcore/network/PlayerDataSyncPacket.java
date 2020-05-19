package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PlayerDataSyncPacket {

    UUID targetUUID;
    CompoundNBT updateTag;

    public PlayerDataSyncPacket(MKPlayerData player, UUID targetUUID) {
        this.targetUUID = targetUUID;
        updateTag = new CompoundNBT();
        player.serializeClientUpdate(updateTag);
    }


    public PlayerDataSyncPacket(PacketBuffer buffer) {
        targetUUID = buffer.readUniqueId();
        updateTag = buffer.readCompoundTag();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeUniqueId(targetUUID);
        buffer.writeCompoundTag(updateTag);
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
                    ((MKPlayerData) cap).deserializeClientUpdate(updateTag);
                }
            });
        });
        ctx.setPacketHandled(true);
    }
}
