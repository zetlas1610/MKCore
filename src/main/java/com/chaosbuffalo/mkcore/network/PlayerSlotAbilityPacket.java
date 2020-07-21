package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.core.AbilitySlot;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerSlotAbilityPacket {

    private final AbilitySlot type;
    private final ResourceLocation ability;
    private final int slotIndex;

    public PlayerSlotAbilityPacket(AbilitySlot type, int slotIndex, ResourceLocation ability) {
        this.type = type;
        this.slotIndex = slotIndex;
        this.ability = ability;
    }


    public PlayerSlotAbilityPacket(PacketBuffer buf) {
        ability = buf.readResourceLocation();
        type = buf.readEnumValue(AbilitySlot.class);
        slotIndex = buf.readInt();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeResourceLocation(ability);
        buf.writeEnumValue(type);
        buf.writeInt(slotIndex);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity entity = ctx.getSender();
            if (entity == null) {
                return;
            }
            entity.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(playerData ->
                    playerData.getKnowledge().getAbilityContainer(type).setAbilityInSlot(slotIndex, ability));
        });
        ctx.setPacketHandled(true);
    }
}
