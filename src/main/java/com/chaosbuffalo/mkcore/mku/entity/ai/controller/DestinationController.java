package com.chaosbuffalo.mkcore.mku.entity.ai.controller;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKMemoryModuleTypes;
import com.chaosbuffalo.mkcore.mku.entity.ai.sensor.DestinationSensor;
import net.minecraft.entity.LivingEntity;

public class DestinationController {


    public static void enterMeleeMode(LivingEntity entity, double meleeDistance){
        entity.getBrain().setMemory(MKMemoryModuleTypes.DESTINATION_MOVEMENT,
                DestinationSensor.MovementType.FOLLOW);
        entity.getBrain().setMemory(MKMemoryModuleTypes.TARGET_DISTANCE, meleeDistance);
        MKCore.LOGGER.info("Entering melee mode {} {}", entity,
                entity.getBrain().getMemory(MKMemoryModuleTypes.DESTINATION_MOVEMENT));
    }

    public static void enterStationary(LivingEntity entity){
        entity.getBrain().setMemory(MKMemoryModuleTypes.DESTINATION_MOVEMENT,
                DestinationSensor.MovementType.STATIONARY);
    }

    public static void enterCastingMode(LivingEntity entity, double castingDistance){
        entity.getBrain().setMemory(MKMemoryModuleTypes.DESTINATION_MOVEMENT,
                DestinationSensor.MovementType.KITE);
        entity.getBrain().setMemory(MKMemoryModuleTypes.TARGET_DISTANCE, castingDistance);
    }
}
