package com.chaosbuffalo.mkcore.mku.entity.ai.sensor;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityTarget;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityUseContext;
import com.chaosbuffalo.mkcore.mku.entity.MKEntity;
import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKMemoryModuleTypes;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

public class AbilityUseSensor extends Sensor<MKEntity> {

    public AbilityUseSensor(){
        super(40);
    }

    @Override
    protected void update(ServerWorld worldIn, MKEntity entityIn) {
        Optional<MKAbility> abilityOptional = entityIn.getBrain().getMemory(MKMemoryModuleTypes.CURRENT_ABILITY);
        Optional<LivingEntity> targetOptional = entityIn.getBrain().getMemory(MKMemoryModuleTypes.THREAT_TARGET);
        if (targetOptional.isPresent() && !abilityOptional.isPresent()){
            entityIn.getCapability(Capabilities.ENTITY_CAPABILITY).ifPresent(mkEntityData -> {
                AbilityUseContext context = new AbilityUseContext(entityIn, targetOptional.get(),
                        entityIn.getBrain().getMemory(MKMemoryModuleTypes.ALLIES).orElse(new ArrayList<>()),
                        entityIn.getBrain().getMemory(MKMemoryModuleTypes.ENEMIES).orElse(new ArrayList<>()));
                for (MKAbilityInfo ability : mkEntityData.getKnowledge().getAbilities()){
                    MKAbility mkAbility = ability.getAbility();
                    if (mkEntityData.getAbilityExecutor().canActivateAbility(mkAbility)){
                        if (mkAbility.getAIShouldUse(context)){
                            AbilityTarget target = mkAbility.getAbilityTarget(context);
                            if (target != null){
                                entityIn.getBrain().setMemory(MKMemoryModuleTypes.CURRENT_ABILITY, mkAbility);
                                entityIn.getBrain().setMemory(MKMemoryModuleTypes.ABILITY_TARGET,
                                        target.getTargetEntity());
                                entityIn.getBrain().setMemory(MKMemoryModuleTypes.MOVEMENT_STRATEGY,
                                        target.getMovementStrategy());
                                entityIn.getBrain().setMemory(MKMemoryModuleTypes.MOVEMENT_TARGET,
                                        target.getTargetEntity());
                                return;
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public Set<MemoryModuleType<?>> getUsedMemories() {
        return ImmutableSet.of(MKMemoryModuleTypes.CURRENT_ABILITY, MKMemoryModuleTypes.THREAT_TARGET,
                MKMemoryModuleTypes.ABILITY_TARGET, MKMemoryModuleTypes.ALLIES, MKMemoryModuleTypes.ENEMIES,
                MKMemoryModuleTypes.MOVEMENT_STRATEGY);
    }
}
