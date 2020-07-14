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
    private final int slot;
    private static final int NO_SLOT_REQUIRED = -1;

    public PlayerLearnAbilityRequestPacket(ResourceLocation abilityId, int slot, int entityId) {
        this.entityId = entityId;
        this.slot = slot;
        this.abilityId = abilityId;
    }

    public PlayerLearnAbilityRequestPacket(ResourceLocation abilityId, int entityId){
        this(abilityId, NO_SLOT_REQUIRED, entityId);
    }


    public PlayerLearnAbilityRequestPacket(PacketBuffer buffer) {
        entityId = buffer.readInt();
        abilityId = buffer.readResourceLocation();
        slot = buffer.readInt();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(entityId);
        buffer.writeResourceLocation(abilityId);
        buffer.writeInt(slot);
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
                    if (slot == NO_SLOT_REQUIRED){
                        playerData.getKnowledge().learnAbility(ability);
                    } else {
                        playerData.getKnowledge().slotAbility(ability, slot);
                    }
                });
            }
        });
        ctx.setPacketHandled(true);
    }
}
