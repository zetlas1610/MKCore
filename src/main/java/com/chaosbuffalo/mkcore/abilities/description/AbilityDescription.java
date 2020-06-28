package com.chaosbuffalo.mkcore.abilities.description;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.util.text.ITextComponent;

import java.util.function.BiFunction;

public class AbilityDescription<T extends MKAbility> {
    private final BiFunction<T, IMKEntityData, ITextComponent> entitySpecificDescription;
    private final T ability;

    public AbilityDescription(T ability, BiFunction<T, IMKEntityData, ITextComponent> entitySpecificDescription){
        this.entitySpecificDescription = entitySpecificDescription;
        this.ability = ability;
    }


    public ITextComponent getDescriptionForEntity(IMKEntityData entityData){
        return entitySpecificDescription.apply(ability, entityData);
    }
}
