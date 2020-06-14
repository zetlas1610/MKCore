package com.chaosbuffalo.mkcore.mku.client.render.casting_animations;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.mku.entity.MKEntity;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.util.math.MathHelper;

public class BipedCastAnimation extends AdditionalBipedAnimation<MKEntity> {

    public BipedCastAnimation(BipedModel<?> model) {
        super(model);
    }

    @Override
    public void apply(MKEntity entity) {
        BipedModel<?> model = getModel();
        entity.getCapability(Capabilities.ENTITY_CAPABILITY).ifPresent(mkEntityData -> {
            if (mkEntityData.getAbilityExecutor().isCasting()) {
                int castTicks = mkEntityData.getAbilityExecutor().getCastTicks();
                float castProgress = castTicks / 20.0f;
                float armZ = MathHelper.sin((float) (Math.PI / 2.0f + castProgress * (float) Math.PI / 2.f)) * 1.0f * (float) Math.PI / 4.0f;
                float angle = (float) ((float) (Math.PI / 2.0f) + MathHelper.sin((float) (castProgress * Math.PI)) * (Math.PI / 8.0f));
                model.bipedRightArm.rotateAngleY = 0.0F;
                model.bipedLeftArm.rotateAngleY = 0.0F;
                model.bipedRightArm.rotateAngleZ = -armZ;
                model.bipedLeftArm.rotateAngleZ = armZ;
                model.bipedRightArm.rotateAngleX = -angle;
                model.bipedLeftArm.rotateAngleX = -angle;
            }
        });
    }
}
