package com.chaosbuffalo.mkcore.client.rendering;

import com.chaosbuffalo.mkcore.client.rendering.model.MKPlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.PlayerRenderer;

public class MKPlayerRenderer extends PlayerRenderer {
    public MKPlayerRenderer(EntityRendererManager renderManager, boolean useSmallArms) {
        super(renderManager, useSmallArms);
        this.entityModel = new MKPlayerModel(0.0f, useSmallArms);
    }
}
