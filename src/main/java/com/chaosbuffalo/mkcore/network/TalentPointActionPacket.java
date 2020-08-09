package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.CoreCapabilities;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class TalentPointActionPacket {
    private final ResourceLocation talentTree;
    private final String line;
    private final int index;
    private final Action action;

    public enum Action {
        SPEND,
        REFUND
    }

    public TalentPointActionPacket(ResourceLocation tree, String line, int index, Action action) {
        talentTree = tree;
        this.line = line;
        this.index = index;
        this.action = action;
    }

    public TalentPointActionPacket(PacketBuffer buffer) {
        talentTree = buffer.readResourceLocation();
        line = buffer.readString();
        index = buffer.readVarInt();
        action = buffer.readEnumValue(Action.class);
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeResourceLocation(talentTree);
        buffer.writeString(line);
        buffer.writeVarInt(index);
        buffer.writeEnumValue(action);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity entity = ctx.getSender();
            if (entity == null)
                return;

            entity.getCapability(CoreCapabilities.PLAYER_CAPABILITY).ifPresent(cap -> {
                if (action == Action.SPEND) {
                    cap.getKnowledge().getTalentKnowledge().spendTalentPoint(talentTree, line, index);
                } else if (action == Action.REFUND) {
                    cap.getKnowledge().getTalentKnowledge().refundTalentPoint(talentTree, line, index);
                }
            });
        });
        ctx.setPacketHandled(true);
    }
}
