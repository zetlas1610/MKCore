package com.chaosbuffalo.mkcore.client.rendering.animations;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public abstract class BipedCompleteCastAnimation<T extends LivingEntity> extends AdditionalBipedAnimation<T> {
    private static final float ANIM_TIME = 15.0f;

    public BipedCompleteCastAnimation(BipedModel<?> model) {
        super(model);
    }

    protected abstract int getCastAnimTimer(T entity);

    @Override
    public void apply(T entity) {
        BipedModel<?> model = getModel();
        int castTicks = getCastAnimTimer(entity);
        float progress = ANIM_TIME - castTicks / ANIM_TIME;
        float armZ = MathHelper.cos((float) (Math.PI / 2.0f + progress * (float) Math.PI)) * (float) Math.PI / 2.0f;
        model.bipedRightArm.rotateAngleY = 0.0F;
        model.bipedLeftArm.rotateAngleY = 0.0F;
        model.bipedRightArm.rotateAngleZ = -armZ;
        model.bipedLeftArm.rotateAngleZ = armZ;
        model.bipedRightArm.rotateAngleX = 0.0F;
        model.bipedLeftArm.rotateAngleX = 0.0F;

    }
}
