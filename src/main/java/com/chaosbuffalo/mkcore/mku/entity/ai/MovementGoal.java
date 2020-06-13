package com.chaosbuffalo.mkcore.mku.entity.ai;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Optional;

public class MovementGoal extends Goal {

    @Nullable
    private Path path;
    @Nullable
    private BlockPos blockPos;
    private float speed;
    private final MobEntity entity;

    public MovementGoal(CreatureEntity creature) {
        this.entity = creature;
        speed = 1.0f;
        setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
    }


    @Override
    public boolean shouldExecute() {
        Brain<?> brain = entity.getBrain();
        Optional<WalkTarget> targetOpt = brain.getMemory(MemoryModuleType.WALK_TARGET);
        if (targetOpt.isPresent()) {
            WalkTarget walkTarget = targetOpt.get();
            if (!this.hasReachedTarget(walkTarget)) {
                this.blockPos = walkTarget.getTarget().getBlockPos();
                Path path = entity.getNavigator().getPathToPos(blockPos, 0);
                this.speed = walkTarget.getSpeed();
                if (this.path != path) {
                    this.path = path;
                    entity.getNavigator().setPath(path, speed);
                    brain.setMemory(MemoryModuleType.PATH, path);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        Brain<?> brain = entity.getBrain();
        Optional<WalkTarget> targetOpt = brain.getMemory(MemoryModuleType.WALK_TARGET);
        if (targetOpt.isPresent()) {
            WalkTarget walkTarget = targetOpt.get();
            if (this.entity.getNavigator().noPath()) {
                return false;
            }
            if (hasReachedTarget(walkTarget)) {
                return false;
            }
            return this.blockPos == null || this.blockPos.equals(walkTarget.getTarget().getBlockPos());
        }
        return false;
    }

    @Override
    public void resetTask() {
        super.resetTask();
        entity.getNavigator().clearPath();
        entity.getBrain().removeMemory(MemoryModuleType.WALK_TARGET);
        entity.getBrain().removeMemory(MemoryModuleType.PATH);
        this.path = null;
        this.blockPos = null;
    }


    public void tick() {

    }


    private boolean hasReachedTarget(WalkTarget target) {
        return target.getTarget().getBlockPos().manhattanDistance(new BlockPos(entity)) <= target.getDistance();
    }


}
