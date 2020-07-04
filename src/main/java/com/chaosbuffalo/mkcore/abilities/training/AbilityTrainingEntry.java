package com.chaosbuffalo.mkcore.abilities.training;

import com.chaosbuffalo.mkcore.abilities.MKAbility;

import java.util.ArrayList;
import java.util.List;

public class AbilityTrainingEntry {

    MKAbility ability;
    List<IAbilityLearnRequirement> requirementList;

    public AbilityTrainingEntry(MKAbility ability) {
        this.ability = ability;
        requirementList = new ArrayList<>();
    }

    public MKAbility getAbility() {
        return ability;
    }

    public List<IAbilityLearnRequirement> getRequirements() {
        return requirementList;
    }

    public AbilityTrainingEntry addRequirement(IAbilityLearnRequirement requirement) {
        requirementList.add(requirement);
        return this;
    }
}
