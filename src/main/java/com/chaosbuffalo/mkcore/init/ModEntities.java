package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.entities.EntityMKAreaEffect;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> evt) {
        evt.getRegistry().register(EntityType.Builder.<EntityMKAreaEffect>create(EntityMKAreaEffect::new, EntityClassification.MISC)
                .immuneToFire()
                .size(0, 0)
                .setTrackingRange(64)
                .setUpdateInterval(10)
                .setShouldReceiveVelocityUpdates(true)
                .build("mk_area_effect")
                .setRegistryName(MKCore.makeRL("mk_area_effect")));
    }
}
