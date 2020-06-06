package com.chaosbuffalo.mkcore.mku.entity.ai.movement_strategy;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKMemoryModuleTypes;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;

import java.util.Optional;

public class KiteMovementStrategy extends MovementStrategy {

    private double dist;

    public KiteMovementStrategy(double dist){
        this.dist = dist;
    }

    @Override
    public void update(ServerWorld world, CreatureEntity entity) {
        Brain<?> brain = entity.getBrain();
        Optional<LivingEntity> targetOpt = brain.getMemory(MKMemoryModuleTypes.THREAT_TARGET);
        Optional<WalkTarget> walkTargetOptional = brain.getMemory(MemoryModuleType.WALK_TARGET);
        if (targetOpt.isPresent()){
            LivingEntity target = targetOpt.get();
            WalkTarget walkTarget = walkTargetOptional.orElse(null);
            Vec3d targetPos = null;
            double distToWalkTarget = 0.0;
            double distanceTo = entity.getDistance(target);
            if (walkTarget != null){
                distToWalkTarget = target.getDistanceSq(walkTarget.getTarget().getPos());
            }
            double threeQuarterDist = .75 * dist;
            if (distanceTo < threeQuarterDist && distToWalkTarget < (threeQuarterDist * threeQuarterDist)) {
                targetPos = RandomPositionGenerator.findRandomTargetBlockAwayFrom(
                        entity, (int) Math.round(dist), 3, target.getPositionVec());
                MKCore.LOGGER.info("Target move to: {}, {}", targetPos, dist);
            } else if (distanceTo > 1.1 * dist){
                targetPos = target.getPositionVector();
            }
            if (targetPos != null){
                brain.setMemory(MemoryModuleType.WALK_TARGET,
                        new WalkTarget(targetPos, 1.1f, 1));
            }
        }

    }
}
