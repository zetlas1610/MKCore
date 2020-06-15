package com.chaosbuffalo.mkcore.mku.entity.ai.controller;

import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKUMemoryModuleTypes;
import com.chaosbuffalo.mkcore.mku.entity.ai.movement_strategy.FollowMovementStrategy;
import com.chaosbuffalo.mkcore.mku.entity.ai.movement_strategy.KiteMovementStrategy;
import com.chaosbuffalo.mkcore.mku.entity.ai.movement_strategy.StationaryMovementStrategy;
import net.minecraft.entity.LivingEntity;

public class MovementStrategyController {


    public static void enterMeleeMode(LivingEntity entity, int meleeDistance) {
        entity.getBrain().setMemory(MKUMemoryModuleTypes.MOVEMENT_STRATEGY,
                new FollowMovementStrategy(1.0f, meleeDistance));
    }

    public static void enterStationary(LivingEntity entity) {
        entity.getBrain().setMemory(MKUMemoryModuleTypes.MOVEMENT_STRATEGY,
                new StationaryMovementStrategy());
    }

    public static void enterCastingMode(LivingEntity entity, double castingDistance) {
        entity.getBrain().setMemory(MKUMemoryModuleTypes.MOVEMENT_STRATEGY,
                new KiteMovementStrategy(castingDistance));
    }
}
