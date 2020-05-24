package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.abilities.PlayerAbility;
import com.chaosbuffalo.mkcore.abilities.PlayerAbilityInfo;
import com.chaosbuffalo.mkcore.core.IMKPlayerData;
import net.minecraftforge.eventbus.api.Cancelable;

public class PlayerAbilityEvent extends PlayerDataEvent {
    private final PlayerAbilityInfo abilityInfo;

    private PlayerAbilityEvent(IMKPlayerData data, PlayerAbilityInfo abilityInfo) {
        super(data);
        this.abilityInfo = abilityInfo;
    }

    public PlayerAbility getAbility() {
        return abilityInfo.getAbility();
    }

    public PlayerAbilityInfo getAbilityInfo() {
        return abilityInfo;
    }

    public static class Completed extends PlayerAbilityEvent {

        public Completed(IMKPlayerData data, PlayerAbilityInfo abilityInfo) {
            super(data, abilityInfo);
        }
    }

    @Cancelable
    public static class StartCasting extends PlayerAbilityEvent {

        public StartCasting(IMKPlayerData data, PlayerAbilityInfo abilityInfo) {
            super(data, abilityInfo);
        }
    }
}
