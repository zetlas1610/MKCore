package com.chaosbuffalo.mkcore.client.rendering.animations;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;

public abstract class AdditionalBipedAnimation<T extends LivingEntity> extends AdditionalAnimation<T>{

    public AdditionalBipedAnimation(BipedModel<?> model){
        super(model);
    }

    public BipedModel<?> getModel(){
        return (BipedModel<?>) super.getModel();
    }
}
