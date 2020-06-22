package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.Capabilities;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerSlotAbilityPacket {

    private final ResourceLocation ability;
    private final int slotIndex;

    public PlayerSlotAbilityPacket(int slotIndex, ResourceLocation ability) {
        this.slotIndex = slotIndex;
        this.ability = ability;
    }


    public PlayerSlotAbilityPacket(PacketBuffer buf) {
        ability = buf.readResourceLocation();
        slotIndex = buf.readInt();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeResourceLocation(ability);
        buf.writeInt(slotIndex);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity entity = ctx.getSender();
            if (entity == null){
                return;
            }
            entity.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(playerData -> {
                playerData.getKnowledge().getActionBar().setAbilityInSlot(slotIndex, ability);
            });
        });
        ctx.setPacketHandled(true);
    }
}
