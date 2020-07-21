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
    private final ResourceLocation replacingId;

    public PlayerLearnAbilityRequestPacket(ResourceLocation abilityId, ResourceLocation replacingId, int entityId) {
        this.entityId = entityId;
        this.abilityId = abilityId;
        this.replacingId = replacingId;
    }

    public PlayerLearnAbilityRequestPacket(ResourceLocation abilityId, int entityId) {
        this(abilityId, MKCoreRegistry.INVALID_ABILITY, entityId);
    }


    public PlayerLearnAbilityRequestPacket(PacketBuffer buffer) {
        entityId = buffer.readInt();
        abilityId = buffer.readResourceLocation();
        replacingId = buffer.readBoolean() ? buffer.readResourceLocation() : MKCoreRegistry.INVALID_ABILITY;
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(entityId);
        buffer.writeResourceLocation(abilityId);
        if (!replacingId.equals(MKCoreRegistry.INVALID_ABILITY)) {
            buffer.writeBoolean(true);
            buffer.writeResourceLocation(replacingId);
        } else {
            buffer.writeBoolean(false);
        }
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
            if (teacher instanceof IAbilityTrainingEntity) {
                IAbilityTrainer abilityTrainer = ((IAbilityTrainingEntity) teacher).getAbilityTrainer();

                entity.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(playerData -> {
                    AbilityTrainingEntry entry = abilityTrainer.getTrainingEntry(ability);
                    if (!entry.getRequirements().stream().allMatch(r -> r.check(playerData, ability))) {
                        MKCore.LOGGER.debug("Failed to learn ability {} from {} - unmet requirements", abilityId, teacher);
                        return;
                    }

                    entry.getRequirements().forEach(r -> r.onLearned(playerData, ability));
                    playerData.getKnowledge().learnAbility(ability, replacingId);
                });
            }
        });
        ctx.setPacketHandled(true);
    }
}
