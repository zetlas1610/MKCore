package com.chaosbuffalo.mkcore.mku.client.render;

import com.chaosbuffalo.mkcore.mku.entity.GreenLadyEntity;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.util.ResourceLocation;

public class GreenLadyRenderer extends BipedRenderer<GreenLadyEntity, BipedModel<GreenLadyEntity>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("mkultra", "textures/entity/green_lady.png");

    public GreenLadyRenderer(EntityRendererManager rendererManager) {
        super(rendererManager, new MKBipedModel<>(0.0F, 0.0f, 64, 64), 0.5f);
        addLayer(new BipedArmorLayer<>(this, new MKBipedModel<>(0.5F), new MKBipedModel<>(1.0F)));
    }

    @Override
    public ResourceLocation getEntityTexture(GreenLadyEntity entity) {
        return TEXTURE;
    }
}
