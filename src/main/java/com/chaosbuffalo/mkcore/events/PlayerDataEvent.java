package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.eventbus.api.Event;

public class PlayerDataEvent extends Event {
    private final IMKEntityData data;

    protected PlayerDataEvent(IMKEntityData data) {
        this.data = data;
    }

    public PlayerEntity getPlayer() {
        return getPlayerData().getPlayer();
    }

    public IMKEntityData getPlayerData() {
        return data;
    }
}
