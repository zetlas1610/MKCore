package com.chaosbuffalo.mkcore.mku.client.render.casting_animations;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;

public abstract class AdditionalBipedAnimation<T extends LivingEntity> extends AdditionalAnimation<T>{
    private final BipedModel<?> bipedModel;

    public AdditionalBipedAnimation(BipedModel<?> model){
        super(model);
        this.bipedModel = model;
    }

    public BipedModel<?> getModel(){
        return bipedModel;
    }


}
