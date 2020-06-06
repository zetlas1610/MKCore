package com.chaosbuffalo.mkcore.mku.entity.ai.sensor;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKMemoryModuleTypes;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;

import java.util.Optional;
import java.util.Set;

public class DestinationSensor extends Sensor<CreatureEntity> {

    public enum MovementType {
        STATIONARY,
        FOLLOW,
        KITE,
    }

    @Override
    protected void update(ServerWorld worldIn, CreatureEntity entityIn) {
        Brain<?> brain = entityIn.getBrain();
        Optional<LivingEntity> targetOpt = brain.getMemory(MKMemoryModuleTypes.THREAT_TARGET);
        Optional<Double> distOpt = brain.getMemory(MKMemoryModuleTypes.TARGET_DISTANCE);
        Optional<MovementType> moveOpt = brain.getMemory(MKMemoryModuleTypes.DESTINATION_MOVEMENT);
        if (moveOpt.isPresent() && targetOpt.isPresent()){
            double dist = distOpt.orElse(1.0);
            MovementType moveType = moveOpt.get();
            LivingEntity target = targetOpt.get();
            double distanceTo = entityIn.getDistance(target);
            if (moveType == MovementType.STATIONARY){
                if (brain.hasMemory(MemoryModuleType.WALK_TARGET)){
                    brain.removeMemory(MemoryModuleType.WALK_TARGET);
                }
            } else if (moveType == MovementType.FOLLOW){
                brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(target.getPosition(), 1.0f, 1));
            } else if (moveType == MovementType.KITE){
                Vec3d targetPos = null;
                if (distanceTo < .75 * dist) {
                    targetPos = RandomPositionGenerator.findRandomTargetBlockAwayFrom(
                            entityIn, (int) Math.round(dist), 3, target.getPositionVec());

                } else if (distanceTo > .9 * dist){
                    targetPos = target.getPositionVector();
                }
                if (targetPos != null){
                    brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(targetPos, 1.0f, 1));
                }
            }

        }
    }

    @Override
    public Set<MemoryModuleType<?>> getUsedMemories() {
        return ImmutableSet.of(MKMemoryModuleTypes.THREAT_TARGET, MKMemoryModuleTypes.VISIBLE_ENEMIES,
                MemoryModuleType.WALK_TARGET, MKMemoryModuleTypes.DESTINATION_MOVEMENT,
                MKMemoryModuleTypes.TARGET_DISTANCE);
    }
}
