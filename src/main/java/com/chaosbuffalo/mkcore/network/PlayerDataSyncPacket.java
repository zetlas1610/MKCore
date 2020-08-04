package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.Capabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        World world = Minecraft.getInstance().world;
        if (world == null) {
            return;
        }
        PlayerEntity entity = world.getPlayerByUuid(targetUUID);
        if (entity == null)
            return;

        entity.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(cap ->
                cap.getUpdateEngine().deserializeUpdate(updateTag, privateUpdate));
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(this::handleClient);
        ctx.setPacketHandled(true);
    }

    public String toString() {
        return String.format("[priv: %b, tag: %s]", privateUpdate, updateTag);
    }
}
