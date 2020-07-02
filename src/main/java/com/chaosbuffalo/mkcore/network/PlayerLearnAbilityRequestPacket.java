package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerLearnAbilityRequestPacket  {
    private final ResourceLocation ability;

    public PlayerLearnAbilityRequestPacket(ResourceLocation abilityId) {
        ability = abilityId;
    }


    public PlayerLearnAbilityRequestPacket(PacketBuffer buffer) {
        ability = buffer.readResourceLocation();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeResourceLocation(ability);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity entity = ctx.getSender();
            if (entity == null)
                return;
            MKAbility mkAbility = MKCoreRegistry.getAbility(ability);
            if (mkAbility != null){
                entity.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(playerData ->
                        playerData.getKnowledge().learnAbility(mkAbility));
            }
        });
        ctx.setPacketHandled(true);
    }
}