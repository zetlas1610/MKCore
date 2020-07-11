package com.chaosbuffalo.mkcore.abilities.training;

import net.minecraft.util.text.ITextComponent;


public class AbilityRequirementEntry {
    public ITextComponent requirementDescription;
    public boolean isMet;

    public AbilityRequirementEntry(ITextComponent description, boolean isMet){
        this.requirementDescription = description;
        this.isMet = isMet;
    }

}
