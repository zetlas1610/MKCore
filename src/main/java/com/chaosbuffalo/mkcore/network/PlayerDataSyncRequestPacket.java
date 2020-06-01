package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerDataSyncRequestPacket {

    public PlayerDataSyncRequestPacket() {
    }


    public PlayerDataSyncRequestPacket(PacketBuffer buffer) {
    }

    public void toBytes(PacketBuffer buffer) {
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity entity = ctx.getSender();
            if (entity == null)
                return;

            entity.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(MKPlayerData::initialSync);
        });
        ctx.setPacketHandled(true);
    }
}
