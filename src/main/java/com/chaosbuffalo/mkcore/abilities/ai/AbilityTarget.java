package com.chaosbuffalo.mkcore.abilities.ai;

import com.chaosbuffalo.mkcore.mku.entity.ai.movement_strategy.MovementStrategy;
import com.chaosbuffalo.mkcore.mku.entity.ai.movement_strategy.StationaryMovementStrategy;
import net.minecraft.entity.LivingEntity;

public class AbilityTarget {
    private final LivingEntity target;
    private final MovementStrategy movementStrategy;
    private static final MovementStrategy STATIONARY = new StationaryMovementStrategy();

    public AbilityTarget(LivingEntity target, MovementStrategy movementStrategy) {
        this.target = target;
        this.movementStrategy = movementStrategy;
    }

    public AbilityTarget(LivingEntity target) {
        this(target, STATIONARY);
    }

    public LivingEntity getTargetEntity() {
        return target;
    }

    public MovementStrategy getMovementStrategy() {
        return movementStrategy;
    }
}
