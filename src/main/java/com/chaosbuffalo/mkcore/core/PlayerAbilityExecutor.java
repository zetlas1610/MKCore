package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.MKAbility;

public class PlayerAbilityExecutor extends AbilityExecutor {
    public PlayerAbilityExecutor(MKPlayerData playerData) {
        super(playerData);
    }

    @Override
    protected void consumeResource(MKAbility ability) {
        float manaCost = playerData.getStats().getAbilityManaCost(ability.getAbilityId());
        playerData.getStats().consumeMana(manaCost);
    }
}
