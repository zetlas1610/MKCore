package com.chaosbuffalo.mkcore.client.rendering.animations;

import com.chaosbuffalo.mkcore.mku.entity.MKEntity;
import net.minecraft.client.renderer.entity.model.BipedModel;

public class MKEntityCompleteCastAnimation extends BipedCompleteCastAnimation<MKEntity> {
    public MKEntityCompleteCastAnimation(BipedModel<?> model) {
        super(model);
    }

    @Override
    protected int getCastAnimTimer(MKEntity entity) {
        return entity.getCastAnimTimer();
    }
}
