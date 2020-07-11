package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.training.AbilityRequirementEntry;
import com.chaosbuffalo.mkcore.abilities.training.IAbilityLearnRequirement;
import com.chaosbuffalo.mkcore.abilities.training.IAbilityTrainer;
import com.chaosbuffalo.mkcore.client.gui.LearnAbilitiesScreen;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class OpenLearnAbilitiesGuiPacket {
    private final int entityId;
    private final Map<MKAbility, List<AbilityRequirementEntry>> abilityInfo;

    public OpenLearnAbilitiesGuiPacket(MKPlayerData playerData, IAbilityTrainer trainingEntity) {
        this.abilityInfo = new HashMap<>();
        entityId = trainingEntity.getEntityId();
        trainingEntity.getTrainableAbilities(playerData).forEach(entry -> {
            abilityInfo.put(entry.getAbility(), entry.getRequirements()
                    .stream()
                    .map(req -> new AbilityRequirementEntry(req.describe(),
                            req.check(playerData, entry.getAbility())))
                    .collect(Collectors.toList())
            );
        });
    }

    public OpenLearnAbilitiesGuiPacket(PacketBuffer buffer) {
        entityId = buffer.readInt();
        int count = buffer.readVarInt();
        this.abilityInfo = new HashMap<>(count);
        for (int i = 0; i < count; i++) {
            ResourceLocation loc = buffer.readResourceLocation();
            MKAbility ability = MKCoreRegistry.getAbility(loc);
            if (ability != null) {
                List<AbilityRequirementEntry> descriptions = new ArrayList<>();
                int descCount = buffer.readVarInt();
                for (int j = 0; j < descCount; j++) {
                    descriptions.add(new AbilityRequirementEntry(buffer.readTextComponent(), buffer.readBoolean()));
                }
                abilityInfo.put(ability, descriptions);
            }
        }
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(entityId);
        buffer.writeVarInt(abilityInfo.size());
        abilityInfo.forEach((key, descriptions) -> {
            buffer.writeResourceLocation(key.getAbilityId());
            buffer.writeVarInt(descriptions.size());
            for (AbilityRequirementEntry description : descriptions) {
                buffer.writeTextComponent(description.requirementDescription);
                buffer.writeBoolean(description.isMet);
            }
        });
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ITextComponent text = new StringTextComponent("Learn Abilities");
            Minecraft.getInstance().displayGuiScreen(new LearnAbilitiesScreen(text, abilityInfo, entityId));
        });
        ctx.setPacketHandled(true);
    }
}
