package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.eventbus.api.Event;

public class PlayerDataEvent extends Event {
    private final MKPlayerData data;

    protected PlayerDataEvent(MKPlayerData data) {
        this.data = data;
    }

    public PlayerEntity getPlayer() {
        return getPlayerData().getEntity();
    }

    public MKPlayerData getPlayerData() {
        return data;
    }
}
