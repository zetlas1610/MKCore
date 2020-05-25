package com.chaosbuffalo.mkcore.client.rendering;

import com.chaosbuffalo.mkcore.entities.EntityMKAreaEffect;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class EntityMKAreaEffectRenderer extends EntityRenderer<EntityMKAreaEffect> {
    public EntityMKAreaEffectRenderer(EntityRendererManager manager) {
        super(manager);
    }

    @Override
    public ResourceLocation getEntityTexture(EntityMKAreaEffect entity) {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
    }

    @Override
    public boolean shouldRender(EntityMKAreaEffect entity, @Nonnull ClippingHelperImpl clipping, double x, double y, double z) {
        return false;
    }
}
