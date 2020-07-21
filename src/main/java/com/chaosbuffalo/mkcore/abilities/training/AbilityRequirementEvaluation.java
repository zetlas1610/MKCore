package com.chaosbuffalo.mkcore.abilities.training;

import net.minecraft.util.text.ITextComponent;


public class AbilityRequirementEvaluation {
    public ITextComponent requirementDescription;
    public boolean isMet;

    public AbilityRequirementEvaluation(ITextComponent description, boolean isMet) {
        this.requirementDescription = description;
        this.isMet = isMet;
    }

}
