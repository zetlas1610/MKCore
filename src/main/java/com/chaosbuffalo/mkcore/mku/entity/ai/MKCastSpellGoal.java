package com.chaosbuffalo.mkcore.mku.entity.ai;

import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;

public class MKCastSpellGoal extends Goal {

    private final CreatureEntity attacker;

    public MKCastSpellGoal(CreatureEntity creature, double speedIn, boolean useLongMemory) {
        this.attacker = creature;
    }

    @Override
    public boolean shouldExecute() {
        return false;
    }

    public boolean shouldContinueExecuting() {
        LivingEntity livingentity = this.attacker.getAttackTarget();
        if (livingentity == null) {
            return false;
        } else if (!livingentity.isAlive()) {
            return false;
        } else if (!this.attacker.isWithinHomeDistanceFromPosition(new BlockPos(livingentity))) {
            return false;
        } else {
            return Targeting.isValidEnemy(attacker, livingentity);
        }
    }
}
