package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingEntry;
import com.chaosbuffalo.mkcore.abilities.training.IAbilityTrainer;
import com.chaosbuffalo.mkcore.abilities.training.IAbilityTrainingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerLearnAbilityRequestPacket {
    private final int entityId;
    private final ResourceLocation abilityId;

    public PlayerLearnAbilityRequestPacket(ResourceLocation abilityId, int entityId) {
        this.entityId = entityId;
        this.abilityId = abilityId;
    }


    public PlayerLearnAbilityRequestPacket(PacketBuffer buffer) {
        entityId = buffer.readInt();
        abilityId = buffer.readResourceLocation();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(entityId);
        buffer.writeResourceLocation(abilityId);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity entity = ctx.getSender();
            if (entity == null)
                return;
            MKAbility ability = MKCoreRegistry.getAbility(abilityId);
            if (ability == null) {
                return;
            }

            Entity teacher = entity.getServerWorld().getEntityByID(entityId);
            MKCore.LOGGER.info("Found ability teacher {}", teacher);
            if (teacher instanceof IAbilityTrainingEntity) {
                IAbilityTrainer abilityTrainer = ((IAbilityTrainingEntity) teacher).getAbilityTrainer();

                entity.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(playerData -> {
                    AbilityTrainingEntry entry = abilityTrainer.getTrainingEntry(ability);
                    if (!entry.getRequirements().stream().allMatch(r -> r.check(playerData, ability))) {
                        MKCore.LOGGER.info("Failed to learn ability {} - unmet requirements", abilityId);
                        return;
                    }

                    entry.getRequirements().forEach(r -> r.onLearned(playerData, ability));
                    playerData.getKnowledge().learnAbility(ability);
                });
            }
        });
        ctx.setPacketHandled(true);
    }
}
