package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.Capabilities;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class AbilityCooldownPacket {

    private final ResourceLocation abilityId;
    private final int cooldown;

    public AbilityCooldownPacket(ResourceLocation abilityId, int cooldown) {
        this.abilityId = abilityId;
        this.cooldown = cooldown;
    }

    public AbilityCooldownPacket(PacketBuffer buffer) {
        abilityId = buffer.readResourceLocation();
        cooldown = buffer.readInt();
    }

    public void toBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        pb.writeResourceLocation(abilityId);
        pb.writeInt(cooldown);
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

            entity.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(cap ->
                    cap.getAbilityExecutor().setCooldown(abilityId, cooldown));
        });
        ctx.setPacketHandled(true);
    }
}
