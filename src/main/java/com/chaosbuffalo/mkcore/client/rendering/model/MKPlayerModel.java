package com.chaosbuffalo.mkcore.client.rendering.model;

import com.chaosbuffalo.mkcore.CoreCapabilities;
import com.chaosbuffalo.mkcore.client.rendering.animations.AdditionalBipedAnimation;
import com.chaosbuffalo.mkcore.client.rendering.animations.BipedCastAnimation;
import com.chaosbuffalo.mkcore.client.rendering.animations.PlayerCompleteCastAnimation;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.player.PlayerEntity;

public class MKPlayerModel extends PlayerModel<AbstractClientPlayerEntity> {
    private final BipedCastAnimation<PlayerEntity> castAnimation = new BipedCastAnimation<>(this);
    private final PlayerCompleteCastAnimation completeCastAnimation = new PlayerCompleteCastAnimation(this);

    public MKPlayerModel(float modelSize, boolean smallArmsIn) {
        super(modelSize, smallArmsIn);
    }

    @Override
    public void setRotationAngles(AbstractClientPlayerEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        entityIn.getCapability(CoreCapabilities.PLAYER_CAPABILITY).ifPresent(mkEntityData -> {
            AdditionalBipedAnimation<PlayerEntity> animation = getAdditionalAnimation(mkEntityData);
            if (animation != null) {
                animation.apply(entityIn);
            }
        });
    }

    public AdditionalBipedAnimation<PlayerEntity> getAdditionalAnimation(MKPlayerData playerData) {
        switch (playerData.getAnimationModule().getPlayerVisualCastState()) {
            case CASTING:
                return castAnimation;
            case RELEASE:
                return completeCastAnimation;
            case NONE:
            default:
                return null;
        }
    }
}
