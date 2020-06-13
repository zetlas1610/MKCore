package com.chaosbuffalo.mkcore.client.rendering.model;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.util.math.MathHelper;

public class MKPlayerModel extends PlayerModel<AbstractClientPlayerEntity> {
    public MKPlayerModel(float modelSize, boolean smallArmsIn) {
        super(modelSize, smallArmsIn);
    }

    @Override
    public void setRotationAngles(AbstractClientPlayerEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        entityIn.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(mkEntityData -> {
            if (mkEntityData.getAbilityExecutor().isCasting()){
                MKCore.LOGGER.info("Rendering player casting");
                int castTicks = mkEntityData.getAbilityExecutor().getCastTicks();
                float castProgress = castTicks / 20.0f;
                float armZ = MathHelper.sin((float) (Math.PI / 2.0f + castProgress * (float)Math.PI / 2.f)) * 1.0f * (float) Math.PI / 4.0f;
                float angle = (float) ((float) (Math.PI / 2.0f) + MathHelper.sin((float) (castProgress * Math.PI)) * (Math.PI / 8.0f));
                bipedRightArm.rotateAngleY = 0.0F;
                bipedLeftArm.rotateAngleY = 0.0F;
                bipedRightArm.rotateAngleZ = -armZ;
                bipedLeftArm.rotateAngleZ = armZ;
                bipedRightArm.rotateAngleX = -angle;
                bipedLeftArm.rotateAngleX = -angle;
            }
        });
    }
}
