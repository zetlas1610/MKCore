package com.chaosbuffalo.mkcore.mku.client.render.casting_animations;

import com.chaosbuffalo.mkcore.mku.entity.MKEntity;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.util.math.MathHelper;

public class BipedCompleteCastAnimation extends AdditionalBipedAnimation<MKEntity> {
    private static final float ANIM_TIME = 15.0f;

    public BipedCompleteCastAnimation(BipedModel<?> model) {
        super(model);
    }

    @Override
    public void apply(MKEntity entity) {
        BipedModel<?> model = getModel();
        int castTicks = entity.getCastAnimTimer();
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
