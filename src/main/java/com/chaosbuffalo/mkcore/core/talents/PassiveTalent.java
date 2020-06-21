package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.abilities.PassiveTalentAbility;
import net.minecraft.util.ResourceLocation;

public class PassiveTalent extends BaseTalent implements IAbilityTalent<PassiveTalentAbility> {
    private final PassiveTalentAbility ability;

    public PassiveTalent(ResourceLocation name, PassiveTalentAbility ability) {
        super(name);
        this.ability = ability;
    }

    @Override
    public PassiveTalentAbility getAbility() {
        return ability;
    }

    @Override
    public TalentType<?> getTalentType() {
        return TalentType.PASSIVE;
    }

    @Override
    public String toString() {
        return "PassiveTalent{" +
                "ability=" + ability +
                '}';
    }
}
