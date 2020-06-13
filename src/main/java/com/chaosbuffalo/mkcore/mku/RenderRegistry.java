package com.chaosbuffalo.mkcore.mku;

import com.chaosbuffalo.mkcore.client.rendering.MKPlayerRenderer;
import com.chaosbuffalo.mkcore.mku.client.render.GreenLadyRenderer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class RenderRegistry {

    public static void registerRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(
                MKUEntityTypes.GREEN_LADY.get(),
                GreenLadyRenderer::new);
        Minecraft.getInstance().getRenderManager().skinMap.put("default",
                new MKPlayerRenderer(Minecraft.getInstance().getRenderManager(), false));
        Minecraft.getInstance().getRenderManager().skinMap.put("slim",
                new MKPlayerRenderer(Minecraft.getInstance().getRenderManager(), true));
    }
}
