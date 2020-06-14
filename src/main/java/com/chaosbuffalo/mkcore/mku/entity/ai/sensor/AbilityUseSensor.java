package com.chaosbuffalo.mkcore.mku.entity.ai.sensor;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.abilities.MKAbilityMemories;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityTarget;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityUseContext;
import com.chaosbuffalo.mkcore.mku.entity.MKEntity;
import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKUMemoryModuleTypes;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.world.server.ServerWorld;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class AbilityUseSensor extends Sensor<MKEntity> {

    public AbilityUseSensor() {
        super(40);
    }

    @Override
    protected void update(ServerWorld worldIn, MKEntity entityIn) {
        Optional<MKAbility> abilityOptional = entityIn.getBrain().getMemory(MKUMemoryModuleTypes.CURRENT_ABILITY);
        Optional<LivingEntity> targetOptional = entityIn.getBrain().getMemory(MKUMemoryModuleTypes.THREAT_TARGET);
        if (!abilityOptional.isPresent()) {
            entityIn.getCapability(Capabilities.ENTITY_CAPABILITY).ifPresent(mkEntityData -> {
                AbilityUseContext context = new AbilityUseContext(entityIn, targetOptional.orElse(null),
                        entityIn.getBrain().getMemory(MKUMemoryModuleTypes.ALLIES).orElse(Collections.emptyList()),
                        entityIn.getBrain().getMemory(MKUMemoryModuleTypes.ENEMIES).orElse(Collections.emptyList()));
                for (MKAbilityInfo ability : mkEntityData.getKnowledge().getAbilitiesPriorityOrder()) {
                    MKAbility mkAbility = ability.getAbility();
                    if (mkEntityData.getAbilityExecutor().canActivateAbility(mkAbility)) {
                        if (mkAbility.shouldAIUse(context)) {
                            AbilityTarget target = mkAbility.getAbilityTarget(context);
                            if (target != null) {
                                entityIn.getBrain().setMemory(MKUMemoryModuleTypes.CURRENT_ABILITY, mkAbility);
                                entityIn.getBrain().setMemory(MKAbilityMemories.ABILITY_TARGET,
                                        target.getTargetEntity());
                                entityIn.getBrain().setMemory(MKUMemoryModuleTypes.MOVEMENT_STRATEGY,
                                        target.getMovementStrategy());
                                entityIn.getBrain().setMemory(MKUMemoryModuleTypes.MOVEMENT_TARGET,
                                        target.getTargetEntity());
                                return;
                            }
                        }
                    }
                }
                entityIn.enterDefaultMovementState(targetOptional.orElse(null));
            });
        }
    }

    @Override
    public Set<MemoryModuleType<?>> getUsedMemories() {
        return ImmutableSet.of(MKUMemoryModuleTypes.CURRENT_ABILITY, MKUMemoryModuleTypes.THREAT_TARGET,
                MKAbilityMemories.ABILITY_TARGET, MKUMemoryModuleTypes.ALLIES, MKUMemoryModuleTypes.ENEMIES,
                MKUMemoryModuleTypes.MOVEMENT_STRATEGY);
    }
}
