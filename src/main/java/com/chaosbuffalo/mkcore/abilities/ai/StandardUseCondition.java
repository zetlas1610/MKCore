package com.chaosbuffalo.mkcore.abilities.ai;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.mku.entity.ai.movement_strategy.KiteMovementStrategy;
import com.chaosbuffalo.mkcore.mku.entity.ai.movement_strategy.MovementStrategy;

import javax.annotation.Nullable;

public class StandardUseCondition extends AbilityUseCondition {
    private final MovementStrategy movementStrategy;

    public StandardUseCondition(MKAbility ability) {
        super(ability);
        movementStrategy = new KiteMovementStrategy(ability.getDistance() * .5);
    }

    @Override
    public boolean test(AbilityUseContext context) {
        float range = getAbility().getDistance();
        return context.getThreatTarget().getDistanceSq(context.getCaster()) <= range * range;
    }

    @Nullable
    @Override
    public AbilityTarget getTarget(AbilityUseContext context) {
        return new AbilityTarget(context.getThreatTarget(), movementStrategy);
    }
}
