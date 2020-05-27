package com.chaosbuffalo.mkcore.events;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class ServerSideLeftClickEmpty extends PlayerEvent {
    private final Hand hand;
    private final BlockPos pos;

    public ServerSideLeftClickEmpty(PlayerEntity player) {
        super(player);
        this.hand = Hand.MAIN_HAND;
        this.pos = new BlockPos(player);
    }

    public Hand getHand() {
        return hand;
    }

    public BlockPos getPos() {
        return pos;
    }
}