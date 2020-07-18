package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraftforge.eventbus.api.Event;

public class MKPlayerEvent extends Event {
    private final MKPlayerData playerData;

    public MKPlayerEvent(MKPlayerData playerData){
        this.playerData = playerData;
    }

    public MKPlayerData getPlayerData() {
        return playerData;
    }
}
