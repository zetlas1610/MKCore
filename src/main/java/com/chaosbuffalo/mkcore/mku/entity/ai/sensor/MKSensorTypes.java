package com.chaosbuffalo.mkcore.mku.entity.ai.sensor;


import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKSensorTypes {


    public static SensorType<ThreatSensor> THREAT_SENSOR;

    public static SensorType<MKLivingEntitiesSensor> ENTITIES_SENSOR;

    public static SensorType<MovementStrategySensor> DESTINATION_SENSOR;

    @SubscribeEvent
    public static void registerModuleTypes(RegistryEvent.Register<SensorType<?>> evt){
        ENTITIES_SENSOR = new SensorType<>(MKLivingEntitiesSensor::new);
        ENTITIES_SENSOR.setRegistryName(MKCore.MOD_ID, "sensor.entities");
        evt.getRegistry().register(ENTITIES_SENSOR);
        THREAT_SENSOR = new SensorType<>(ThreatSensor::new);
        THREAT_SENSOR.setRegistryName(MKCore.MOD_ID, "sensor.threat");
        evt.getRegistry().register(THREAT_SENSOR);
        DESTINATION_SENSOR = new SensorType<>(MovementStrategySensor::new);
        DESTINATION_SENSOR.setRegistryName(MKCore.MOD_ID, "sensor.destination");
        evt.getRegistry().register(DESTINATION_SENSOR);
    }
}
