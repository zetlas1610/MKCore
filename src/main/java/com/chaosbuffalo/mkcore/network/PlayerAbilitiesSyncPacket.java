package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.PlayerAbility;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

public class PlayerAbilitiesSyncPacket {
    private final Map<ResourceLocation, CompoundNBT> data;


    public PlayerAbilitiesSyncPacket(Collection<PlayerAbility> abilities) {
        data = new HashMap<>();
        for (PlayerAbility ability : abilities) {
            data.put(ability.getRegistryName(), ability.serialize());
        }
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(data.size());
        for (Entry<ResourceLocation, CompoundNBT> abilityData : data.entrySet()) {
            buffer.writeResourceLocation(abilityData.getKey());
            buffer.writeCompoundTag(abilityData.getValue());
        }
    }

    public PlayerAbilitiesSyncPacket(PacketBuffer buffer) {
        int count = buffer.readInt();
        data = new HashMap<>();
        for (int i = 0; i < count; i++) {
            ResourceLocation abilityName = buffer.readResourceLocation();
            CompoundNBT abilityData = buffer.readCompoundTag();
            data.put(abilityName, abilityData);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        MKCore.LOGGER.info("Handling player abilities update packet");
        ctx.enqueueWork(() -> {
            for (Entry<ResourceLocation, CompoundNBT> abilityData : data.entrySet()) {
                PlayerAbility ability = MKCoreRegistry.ABILITIES.getValue(abilityData.getKey());
                if (ability != null) {
                    MKCore.LOGGER.info("Updating ability with server data: {}", abilityData.getKey());
                    ability.deserialize(abilityData.getValue());
                } else {
                    MKCore.LOGGER.warn("Skipping ability update for {}", abilityData.getKey());
                }

            }
        });
        ctx.setPacketHandled(true);
    }
}
