package com.chaosbuffalo.mkcore.mku.client.render;

import com.chaosbuffalo.mkcore.mku.entity.MKEntity;
import net.minecraft.client.renderer.entity.model.BipedModel;

public class MKBipedModel<T extends MKEntity> extends BipedModel<T> {
    protected MKBipedModel(float modelSize, float yOffsetIn, int textureWidthIn, int textureHeightIn) {
        super(modelSize, yOffsetIn, textureWidthIn, textureHeightIn);
    }

    public MKBipedModel(float modelSize){
        super(modelSize);
    }
}