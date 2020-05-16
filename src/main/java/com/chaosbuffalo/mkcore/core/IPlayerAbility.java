package com.chaosbuffalo.mkcore.core;

import net.minecraft.util.ResourceLocation;

public interface IPlayerAbility {

    public enum AbilityType {
        Active,
        Toggle,
        Passive,
        Ultimate
    }

    ResourceLocation getAbilityId();

    IPlayerAbilityInfo createAbilityInfo();

//    private ResourceLocation abilityId;

//    public PlayerAbility(String domain, String id) {
//        this(new ResourceLocation(domain, id));
//    }
//
//    public PlayerAbility(ResourceLocation abilityId) {
//        this.abilityId = abilityId;
//    }
//
//    public ResourceLocation getAbilityId() {
//        return abilityId;
//    }

//    public PlayerAbilityInfo createAbilityInfo() {
//        return new PlayerAbilityInfo(this);
//    }
}
