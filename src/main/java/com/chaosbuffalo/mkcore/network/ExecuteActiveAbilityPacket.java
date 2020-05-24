package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.Capabilities;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ExecuteActiveAbilityPacket {

    private final int slot;

    public ExecuteActiveAbilityPacket(int slot) {
        this.slot = slot;
    }

    public ExecuteActiveAbilityPacket(PacketBuffer buffer) {
        slot = buffer.readVarInt();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeVarInt(slot);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity entity = ctx.getSender();
            if (entity == null)
                return;

            entity.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(cap -> cap.executeHotBarAbility(slot));
        });
        ctx.setPacketHandled(true);
    }
}
