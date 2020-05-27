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

    private final UUID targetUUID;
    private final boolean privateUpdate;
    private final CompoundNBT updateTag;

    public PlayerDataSyncPacket(UUID targetUUID, CompoundNBT updateTag, boolean privateUpdate) {
        this.targetUUID = targetUUID;
        this.privateUpdate = privateUpdate;
        this.updateTag = updateTag;
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
//        MKCore.LOGGER.info("sync toBytes priv:{} {}", privateUpdate, updateTag);
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
                    ((MKPlayerData) cap).getUpdateEngine().deserializeUpdate(updateTag, privateUpdate);
                }
            });
        });
        ctx.setPacketHandled(true);
    }

    public String toString() {
        return String.format("[priv: %b, tag: %s]", privateUpdate, updateTag);
    }
}
