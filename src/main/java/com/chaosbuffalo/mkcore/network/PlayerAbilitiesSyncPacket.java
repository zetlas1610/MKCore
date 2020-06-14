package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.mojang.datafixers.Dynamic;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
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


    public PlayerAbilitiesSyncPacket(Collection<MKAbility> abilities) {
        data = new HashMap<>();
        for (MKAbility ability : abilities) {
            INBT dyn = ability.serializeDynamic(NBTDynamicOps.INSTANCE);
            if (dyn instanceof CompoundNBT) {
                data.put(ability.getRegistryName(), (CompoundNBT) dyn);
            } else {
                throw new RuntimeException(String.format("Ability %s did not serialize to a CompoundNBT!", ability.getAbilityId()));
            }
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
        MKCore.LOGGER.debug("Handling player abilities update packet");
        ctx.enqueueWork(() -> {
            for (Entry<ResourceLocation, CompoundNBT> abilityData : data.entrySet()) {
                MKAbility ability = MKCoreRegistry.ABILITIES.getValue(abilityData.getKey());
                if (ability != null) {
                    MKCore.LOGGER.debug("Updating ability with server data: {}", abilityData.getKey());
                    ability.deserializeDynamic(new Dynamic<>(NBTDynamicOps.INSTANCE, abilityData.getValue()));
                } else {
                    MKCore.LOGGER.warn("Skipping ability update for {}", abilityData.getKey());
                }

            }
        });
        ctx.setPacketHandled(true);
    }
}
