package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.talents.TalentTreeDefinition;
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
import java.util.function.Supplier;

public class TalentDefinitionSyncPacket {
    private final Map<ResourceLocation, CompoundNBT> data = new HashMap<>();

    public TalentDefinitionSyncPacket(Collection<TalentTreeDefinition> abilities) {
        for (TalentTreeDefinition ability : abilities) {
            INBT serialized = ability.serialize(NBTDynamicOps.INSTANCE);
            if (serialized instanceof CompoundNBT) {
                data.put(ability.getTreeId(), (CompoundNBT) serialized);
            } else {
                throw new IllegalArgumentException("TalentTreeDefinition did not serialize to a CompoundNBT!");
            }
        }
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(data.size());
        for (Map.Entry<ResourceLocation, CompoundNBT> abilityData : data.entrySet()) {
            buffer.writeResourceLocation(abilityData.getKey());
            buffer.writeCompoundTag(abilityData.getValue());
        }
    }

    public TalentDefinitionSyncPacket(PacketBuffer buffer) {
        int count = buffer.readInt();
        for (int i = 0; i < count; i++) {
            ResourceLocation abilityName = buffer.readResourceLocation();
            CompoundNBT abilityData = buffer.readCompoundTag();
            data.put(abilityName, abilityData);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        MKCore.LOGGER.debug("Handling player talent definition update packet");
        ctx.enqueueWork(() -> {
            for (Map.Entry<ResourceLocation, CompoundNBT> abilityData : data.entrySet()) {

                TalentTreeDefinition definition = TalentTreeDefinition.deserialize(abilityData.getKey(), new Dynamic<>(NBTDynamicOps.INSTANCE, abilityData.getValue()));
//                MKCore.LOGGER.info("got talent update for {} - {}", abilityData.getKey(), abilityData.getValue());
                MKCore.getTalentManager().registerTalentTree(definition);

            }
        });
        ctx.setPacketHandled(true);
    }
}
