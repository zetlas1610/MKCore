package com.chaosbuffalo.mkcore.abilities.ai;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.mku.entity.ai.movement_strategy.FollowMovementStrategy;
import com.chaosbuffalo.mkcore.mku.entity.ai.movement_strategy.MovementStrategy;
import net.minecraft.entity.LivingEntity;

import javax.annotation.Nullable;

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

    @Override
    public boolean test(AbilityUseContext context) {
        if (getAbility().canSelfCast() && needsHealing(context.getCaster())) {
            return true;
        }
        return !selfOnly && context.getFriendlies().size() > 0 && needsHealing(context.getFriendlies().get(0));
    }

    @Nullable
    @Override
    public AbilityTarget getTarget(AbilityUseContext context) {
        if (getAbility().canSelfCast() && needsHealing(context.getCaster())) {
            return new AbilityTarget(context.getCaster());
        } else if (!selfOnly) {
            return new AbilityTarget(context.getFriendlies().get(0), movementStrategy);
        }
        return null;
    }
}
