package com.chaosbuffalo.mkcore.mku.client.render.casting_animations;

import net.minecraft.client.renderer.model.Model;
import net.minecraft.entity.LivingEntity;

public abstract class AdditionalAnimation<T extends LivingEntity> {
    private final Model model;

    public AdditionalAnimation(Model model){
        this.model = model;
    }

    public abstract void apply(T entity);

    public Model getModel(){
        return model;
    }
}
