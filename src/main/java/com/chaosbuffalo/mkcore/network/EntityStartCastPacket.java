package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class EntityStartCastPacket {

    private final int entityId;
    private final ResourceLocation abilityId;
    private final int castTicks;

    public EntityStartCastPacket(IMKEntityData entityData, ResourceLocation abilityId, int castTicks) {
        entityId = entityData.getEntity().getEntityId();
        this.abilityId = abilityId;
        this.castTicks = castTicks;
    }

    public EntityStartCastPacket(PacketBuffer buffer) {
        entityId = buffer.readInt();
        abilityId = buffer.readResourceLocation();
        castTicks = buffer.readInt();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(entityId);
        buffer.writeResourceLocation(abilityId);
        buffer.writeInt(castTicks);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            World world = Minecraft.getInstance().world;
            if (world == null)
                return;

            Entity entity = world.getEntityByID(entityId);
            if (entity == null)
                return;
            if (entity instanceof PlayerEntity) {
                entity.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(cap ->
                        cap.getAbilityExecutor().startCastClient(abilityId, castTicks));
            } else {
                entity.getCapability(Capabilities.ENTITY_CAPABILITY).ifPresent(cap ->
                        cap.getAbilityExecutor().startCastClient(abilityId, castTicks));
            }

        });
        ctx.setPacketHandled(true);
    }
}
