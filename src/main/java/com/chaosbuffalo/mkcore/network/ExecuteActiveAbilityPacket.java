package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ExecuteActiveAbilityPacket {

    private final MKAbility.AbilityType type;
    private final int slot;

    public ExecuteActiveAbilityPacket(MKAbility.AbilityType type, int slot) {
        this.type = type;
        this.slot = slot;
    }

    public ExecuteActiveAbilityPacket(PacketBuffer buffer) {
        type = buffer.readEnumValue(MKAbility.AbilityType.class);
        slot = buffer.readVarInt();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeEnumValue(type);
        buffer.writeVarInt(slot);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity entity = ctx.getSender();
            if (entity == null)
                return;

            entity.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(cap ->
                    cap.getAbilityExecutor().executeHotBarAbility(type, slot));
        });
        ctx.setPacketHandled(true);
    }
}
