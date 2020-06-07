package com.chaosbuffalo.mkcore.mku.entity.ai.movement_strategy;

import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKMemoryModuleTypes;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.world.server.ServerWorld;

import java.util.Optional;

public class FollowMovementStrategy extends MovementStrategy {

    private final float movementScale;
    private final int dist;

    public FollowMovementStrategy(float movementScale, int manhattanDist){
        this.movementScale = movementScale;
        this.dist = manhattanDist;
    }

    @Override
    public void update(ServerWorld world, CreatureEntity entity) {
        Brain<?> brain = entity.getBrain();
        Optional<LivingEntity> targetOpt = brain.getMemory(MKMemoryModuleTypes.MOVEMENT_TARGET);
        if (targetOpt.isPresent()){
            LivingEntity target = targetOpt.get();
            brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(target.getPosition(),
                    movementScale, dist));
        }
    }
}
