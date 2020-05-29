package com.chaosbuffalo.mkcore.mku.client.render;

import com.chaosbuffalo.mkcore.mku.entity.GreenLadyEntity;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.ZombieModel;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.util.ResourceLocation;

public class GreenLadyRenderer extends AbstractZombieRenderer<GreenLadyEntity, ZombieModel<GreenLadyEntity>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("mkultra","textures/entity/green_lady.png");

    public GreenLadyRenderer(EntityRendererManager rendererManager){
        super(rendererManager, new ZombieModel<>(0.0F, false),
                new ZombieModel<>(0.5F, true), new ZombieModel<>(1.0F, true));
    }

    @Override
    public ResourceLocation getEntityTexture(ZombieEntity entity) {
        return TEXTURE;
    }
}
