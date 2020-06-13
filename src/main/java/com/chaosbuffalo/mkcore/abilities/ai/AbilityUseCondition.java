package com.chaosbuffalo.mkcore.abilities.ai;

import com.chaosbuffalo.mkcore.abilities.MKAbility;

import javax.annotation.Nullable;

public abstract class AbilityUseCondition {
    private final MKAbility ability;

    public AbilityUseCondition(MKAbility ability){
        this.ability = ability;
    }

    public MKAbility getAbility() {
        return ability;
    }

    public abstract boolean test(AbilityUseContext context);

    @Nullable
    public abstract AbilityTarget getTarget(AbilityUseContext context);
}
