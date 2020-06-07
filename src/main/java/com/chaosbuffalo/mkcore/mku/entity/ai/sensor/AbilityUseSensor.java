package com.chaosbuffalo.mkcore.mku.entity.ai.sensor;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.mku.entity.MKEntity;
import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKMemoryModuleTypes;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.world.server.ServerWorld;

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
                for (MKAbilityInfo ability : mkEntityData.getKnowledge().getAbilities()){
                    if (mkEntityData.getAbilityExecutor().canActivateAbility(ability.getAbility())){
                        entityIn.getBrain().setMemory(MKMemoryModuleTypes.CURRENT_ABILITY, ability.getAbility());
                        return;
                    }
                }
            });
        }
    }

    @Override
    public Set<MemoryModuleType<?>> getUsedMemories() {
        return ImmutableSet.of(MKMemoryModuleTypes.CURRENT_ABILITY, MKMemoryModuleTypes.THREAT_TARGET);
    }
}
