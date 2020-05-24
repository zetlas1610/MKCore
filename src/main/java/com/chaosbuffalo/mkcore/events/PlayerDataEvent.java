package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.core.IMKPlayerData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.eventbus.api.Event;

public class PlayerDataEvent extends Event {
    private final IMKPlayerData data;

    protected PlayerDataEvent(IMKPlayerData data) {
        this.data = data;
    }

    public PlayerEntity getPlayer() {
        return getPlayerData().getPlayer();
    }

    public IMKPlayerData getPlayerData() {
        return data;
    }
}
