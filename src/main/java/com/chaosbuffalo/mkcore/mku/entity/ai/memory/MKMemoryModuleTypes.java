package com.chaosbuffalo.mkcore.mku.entity.ai.memory;


import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.mku.entity.ai.sensor.DestinationSensor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

import java.util.List;
import java.util.Optional;
import java.util.Map;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKMemoryModuleTypes {

    @ObjectHolder("mkcore:allies")
    public static MemoryModuleType<List<LivingEntity>> ALLIES;

    @ObjectHolder("mkcore:enemies")
    public static MemoryModuleType<List<LivingEntity>> ENEMIES;

    @ObjectHolder("mkcore:visible_enemies")
    public static MemoryModuleType<List<LivingEntity>> VISIBLE_ENEMIES;

    @ObjectHolder("mkcore:threat_map")
    public static MemoryModuleType<Map<LivingEntity, ThreatMapEntry>> THREAT_MAP;

    @ObjectHolder("mkcore:threat_list")
    public static MemoryModuleType<List<LivingEntity>> THREAT_LIST;

    @ObjectHolder("mkcore:threat_target")
    public static MemoryModuleType<LivingEntity> THREAT_TARGET;

    @ObjectHolder("mkcore:target_distance")
    public static MemoryModuleType<Double> TARGET_DISTANCE;

    @ObjectHolder("mkcore:destination_movement")
    public static MemoryModuleType<DestinationSensor.MovementType> DESTINATION_MOVEMENT;



    @SubscribeEvent
    public static void registerModuleTypes(RegistryEvent.Register<MemoryModuleType<?>> evt){
        evt.getRegistry().register(new MemoryModuleType<List<LivingEntity>>(Optional.empty())
                .setRegistryName(MKCore.MOD_ID, "allies"));
        evt.getRegistry().register(new MemoryModuleType<List<LivingEntity>>(Optional.empty())
                .setRegistryName(MKCore.MOD_ID, "enemies"));
        evt.getRegistry().register(new MemoryModuleType<List<LivingEntity>>(Optional.empty())
                .setRegistryName(MKCore.MOD_ID, "visible_enemies"));
        evt.getRegistry().register(new MemoryModuleType<Map<LivingEntity, ThreatMapEntry>>(Optional.empty())
                .setRegistryName(MKCore.MOD_ID, "threat_map"));
        evt.getRegistry().register(new MemoryModuleType<List<LivingEntity>>(Optional.empty())
                .setRegistryName(MKCore.MOD_ID, "threat_list"));
        evt.getRegistry().register(new MemoryModuleType<LivingEntity>(Optional.empty())
                .setRegistryName(MKCore.MOD_ID, "threat_target"));
        evt.getRegistry().register(new MemoryModuleType<Double>(Optional.empty())
            .setRegistryName(MKCore.MOD_ID, "target_distance"));
        evt.getRegistry().register(new MemoryModuleType<DestinationSensor.MovementType>(Optional.empty())
            .setRegistryName(MKCore.MOD_ID, "destination_movement"));
    }


}
