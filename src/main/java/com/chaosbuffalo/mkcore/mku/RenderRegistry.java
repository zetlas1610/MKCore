package com.chaosbuffalo.mkcore.mku;

import com.chaosbuffalo.mkcore.mku.MKUEntityTypes;
import com.chaosbuffalo.mkcore.mku.client.render.GreenLadyRenderer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class RenderRegistry {

    public static void registerRenderers(){
        RenderingRegistry.registerEntityRenderingHandler(
                MKUEntityTypes.GREEN_LADY.get(),
                GreenLadyRenderer::new);
    }
}
