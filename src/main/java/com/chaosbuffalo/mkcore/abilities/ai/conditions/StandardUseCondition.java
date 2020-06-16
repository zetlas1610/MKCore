package com.chaosbuffalo.mkcore.abilities.ai.conditions;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityTargetingDecision;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityDecisionContext;
import com.chaosbuffalo.mkcore.mku.entity.ai.movement_strategy.KiteMovementStrategy;
import com.chaosbuffalo.mkcore.mku.entity.ai.movement_strategy.MovementStrategy;
import net.minecraft.entity.LivingEntity;

import javax.annotation.Nonnull;

public class StandardUseCondition extends AbilityUseCondition {
    private final MovementStrategy movementStrategy;

    public StandardUseCondition(MKAbility ability) {
        super(ability);
        movementStrategy = new KiteMovementStrategy(ability.getDistance() * .5);
    }

    @Nonnull
    @Override
    public AbilityTargetingDecision getDecision(AbilityDecisionContext context) {
        LivingEntity threatTarget = context.getThreatTarget();
        if (threatTarget != null && isInRange(context, threatTarget)) {
            return new AbilityTargetingDecision(threatTarget, movementStrategy);
        }
        return AbilityTargetingDecision.UNDECIDED;
    }
}
