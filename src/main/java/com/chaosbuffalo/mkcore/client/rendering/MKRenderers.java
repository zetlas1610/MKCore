package com.chaosbuffalo.mkcore.client.rendering;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.entities.MKAreaEffectEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKRenderers {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent evt) {
        RenderingRegistry.registerEntityRenderingHandler(MKAreaEffectEntity.TYPE, EntityMKAreaEffectRenderer::new);
    }
}
