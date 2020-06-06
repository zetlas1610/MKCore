package com.chaosbuffalo.mkcore.mku.entity.ai.sensor;


import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

import java.util.List;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKSensorTypes {


    public static SensorType<MKThreatSensor> THREAT_SENSOR;

    public static SensorType<MKLivingEntitiesSensor> ENTITIES_SENSOR;

    @SubscribeEvent
    public static void registerModuleTypes(RegistryEvent.Register<SensorType<?>> evt){
        ENTITIES_SENSOR = new SensorType<>(MKLivingEntitiesSensor::new);
        ENTITIES_SENSOR.setRegistryName(MKCore.MOD_ID, "sensor.entities");
        evt.getRegistry().register(ENTITIES_SENSOR);
        THREAT_SENSOR = new SensorType<>(MKThreatSensor::new);
        THREAT_SENSOR.setRegistryName(MKCore.MOD_ID, "sensor.threat");
        evt.getRegistry().register(THREAT_SENSOR);
    }
}
