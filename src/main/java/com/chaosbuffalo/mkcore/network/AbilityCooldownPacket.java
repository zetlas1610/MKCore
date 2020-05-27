package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.core.PlayerAbilityExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AbilityCooldownPacket {

    List<Entry> entries = new ArrayList<>();

    static class Entry {
        final ResourceLocation abilityId;
        final int cooldown;

        Entry(ResourceLocation abilityId, int cooldown) {
            this.abilityId = abilityId;
            this.cooldown = cooldown;
        }
    }

    public AbilityCooldownPacket(ResourceLocation abilityId, int cooldown) {
        entries.add(new Entry(abilityId, cooldown));
    }

    public AbilityCooldownPacket addCooldown(ResourceLocation abilityId, int cooldown) {
        entries.add(new Entry(abilityId, cooldown));
        return this;
    }

    public AbilityCooldownPacket(PacketBuffer buffer) {
        int count = buffer.readVarInt();
        for (int i = 0; i < count; i++) {
            ResourceLocation abilityId = buffer.readResourceLocation();
            int cooldown = buffer.readVarInt();
            entries.add(new Entry(abilityId, cooldown));
        }
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeVarInt(entries.size());

        entries.forEach(e -> {
            buffer.writeResourceLocation(e.abilityId);
            buffer.writeVarInt(e.cooldown);
        });
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

            entity.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(cap -> {
                        PlayerAbilityExecutor executor = cap.getAbilityExecutor();
                        entries.forEach(e -> {
                            executor.setCooldown(e.abilityId, e.cooldown);
                        });
                    }
            );
        });
        ctx.setPacketHandled(true);
    }
}
