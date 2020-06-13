package com.chaosbuffalo.mkcore.mku.client.render;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.mku.entity.MKEntity;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.util.math.MathHelper;

public class MKBipedModel<T extends MKEntity> extends BipedModel<T> {
    protected MKBipedModel(float modelSize, float yOffsetIn, int textureWidthIn, int textureHeightIn) {
        super(modelSize, yOffsetIn, textureWidthIn, textureHeightIn);
    }

    @Override
    public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        entityIn.getCapability(Capabilities.ENTITY_CAPABILITY).ifPresent(mkEntityData -> {
            if (mkEntityData.getAbilityExecutor().isCasting()){
                int castTicks = mkEntityData.getAbilityExecutor().getCastTicks();
                float castProgress = castTicks / 20.0f;
                float armZ = MathHelper.sin((float) (Math.PI / 2.0f + castProgress * (float)Math.PI / 2.f)) * 1.0f * (float) Math.PI / 4.0f;
                float angle = (float) ((float) (Math.PI / 2.0f) + MathHelper.sin((float) (castProgress * Math.PI)) * (Math.PI / 8.0f));
                this.bipedRightArm.rotateAngleY = 0.0F;
                this.bipedLeftArm.rotateAngleY = 0.0F;
                this.bipedRightArm.rotateAngleZ = -armZ;
                this.bipedLeftArm.rotateAngleZ = armZ;
                this.bipedRightArm.rotateAngleX = -angle;
                this.bipedLeftArm.rotateAngleX = -angle;
            }
        });
    }

    public MKBipedModel(float modelSize){
        super(modelSize);
    }
}