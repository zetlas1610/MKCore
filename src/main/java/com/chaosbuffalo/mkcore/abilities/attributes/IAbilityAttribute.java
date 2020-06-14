package com.chaosbuffalo.mkcore.abilities.attributes;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public interface IAbilityAttribute<T> {
    T getValue();

    void setValue(T newValue);

    String getName();

    <D> D serialize(DynamicOps<D> ops);

    <D> void deserialize(Dynamic<D> dynamic);
}
