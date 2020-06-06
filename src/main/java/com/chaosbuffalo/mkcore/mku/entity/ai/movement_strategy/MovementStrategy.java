package com.chaosbuffalo.mkcore.mku.entity.ai.movement_strategy;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.world.server.ServerWorld;

public abstract class MovementStrategy {

    public abstract void update(ServerWorld world, CreatureEntity entity);
}
