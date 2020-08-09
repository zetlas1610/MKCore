package com.chaosbuffalo.mkcore.client.rendering.animations;

import com.chaosbuffalo.mkcore.CoreCapabilities;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.player.PlayerEntity;

public class PlayerCompleteCastAnimation extends BipedCompleteCastAnimation<PlayerEntity> {
    public PlayerCompleteCastAnimation(BipedModel<?> model) {
        super(model);
    }

    @Override
    protected int getCastAnimTimer(PlayerEntity entity) {
        return entity.getCapability(CoreCapabilities.PLAYER_CAPABILITY)
                .map(playerData -> playerData.getAnimationModule().getCastAnimTimer()).orElse(0);
    }
}
