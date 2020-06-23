package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import net.minecraft.util.ResourceLocation;

public class UltimateTalent extends BaseTalent implements IAbilityTalent<MKAbility> {
    private final MKAbility ability;

    public UltimateTalent(ResourceLocation name, MKAbility ability) {
        super(name);
        this.ability = ability;
    }

    @Override
    public MKAbility getAbility() {
        return ability;
    }

    @Override
    public TalentType<?> getTalentType() {
        return TalentType.ULTIMATE;
    }
}
