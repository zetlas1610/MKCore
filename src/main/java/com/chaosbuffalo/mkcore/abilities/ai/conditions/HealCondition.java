package com.chaosbuffalo.mkcore.abilities.ai.conditions;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityTargetingDecision;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityDecisionContext;
import com.chaosbuffalo.mkcore.mku.entity.ai.movement_strategy.FollowMovementStrategy;
import com.chaosbuffalo.mkcore.mku.entity.ai.movement_strategy.MovementStrategy;
import net.minecraft.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.List;

public class HealCondition extends AbilityUseCondition {

    private final float healThreshold;
    private final MovementStrategy movementStrategy;
    private boolean selfOnly;

    public HealCondition(MKAbility ability, float healThreshold) {
        super(ability);
        this.healThreshold = healThreshold;
        this.movementStrategy = new FollowMovementStrategy(1.0f, Math.round(ability.getDistance()));
        selfOnly = false;
    }

    public HealCondition setSelfOnly(boolean selfOnly) {
        this.selfOnly = selfOnly;
        return this;
    }

    public HealCondition(MKAbility ability) {
        this(ability, .75f);
    }

    private boolean needsHealing(LivingEntity entity) {
        return entity.getHealth() <= entity.getMaxHealth() * healThreshold;
    }

    @Nonnull
    @Override
    public AbilityTargetingDecision getDecision(AbilityDecisionContext context) {
        if (getAbility().canSelfCast() && needsHealing(context.getCaster())) {
            return new AbilityTargetingDecision(context.getCaster());
        } else if (!selfOnly) {
            List<LivingEntity> friends = context.getFriendlies();
            for (LivingEntity target : friends) {
                if (needsHealing(target) && isInRange(context, target)) {
                    return new AbilityTargetingDecision(target, movementStrategy);
                }
            }
        }
        return AbilityTargetingDecision.UNDECIDED;
    }
}
