package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerStartCastPacket {

    private final ResourceLocation abilityId;
    private final int castTicks;

    public PlayerStartCastPacket(ResourceLocation abilityId, int castTicks) {
        this.abilityId = abilityId;
        this.castTicks = castTicks;
    }

    public PlayerStartCastPacket(PacketBuffer buffer) {
        abilityId = buffer.readResourceLocation();
        castTicks = buffer.readInt();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeResourceLocation(abilityId);
        buffer.writeInt(castTicks);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            World world = Minecraft.getInstance().world;
            if (world == null)
                return;

            PlayerEntity entity = Minecraft.getInstance().player;
            if (entity == null)
                return;

            entity.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(cap -> ((MKPlayerData) cap).startCastClient(abilityId, castTicks));
        });
        ctx.setPacketHandled(true);
    }
}
