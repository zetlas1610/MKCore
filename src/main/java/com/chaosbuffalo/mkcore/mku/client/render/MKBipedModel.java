package com.chaosbuffalo.mkcore.mku.client.render;


import com.chaosbuffalo.mkcore.mku.client.render.casting_animations.AdditionalBipedAnimation;
import com.chaosbuffalo.mkcore.mku.client.render.casting_animations.BipedCastAnimation;
import com.chaosbuffalo.mkcore.mku.client.render.casting_animations.BipedCompleteCastAnimation;
import com.chaosbuffalo.mkcore.mku.entity.MKEntity;
import net.minecraft.client.renderer.entity.model.BipedModel;

public class MKBipedModel<T extends MKEntity> extends BipedModel<T> {
    private final BipedCastAnimation castAnimation = new BipedCastAnimation(this);
    private final BipedCompleteCastAnimation completeCastAnimation = new BipedCompleteCastAnimation(this);


    protected MKBipedModel(float modelSize, float yOffsetIn, int textureWidthIn, int textureHeightIn) {
        super(modelSize, yOffsetIn, textureWidthIn, textureHeightIn);
    }

    @Override
    public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount,
                                  float ageInTicks, float netHeadYaw, float headPitch) {
        super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        AdditionalBipedAnimation<MKEntity> animation = getAdditionalAnimation(entityIn);
        if (animation != null){
            animation.apply(entityIn);
        }
    }

    public AdditionalBipedAnimation<MKEntity> getAdditionalAnimation(T entityIn){
        switch (entityIn.getVisualCastState()){
            case CASTING:
                return castAnimation;
            case RELEASE:
                return completeCastAnimation;
            case NONE:
            default:
                return null;
        }
    }


    public MKBipedModel(float modelSize) {
        super(modelSize);
    }
}