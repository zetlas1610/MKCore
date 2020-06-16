package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.abilities.MKAbility;

public interface IAbilityTalent<T extends MKAbility> {
    T getAbility();
}
