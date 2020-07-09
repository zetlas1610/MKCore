package com.chaosbuffalo.mkcore.core;

import net.minecraft.entity.ai.attributes.RangedAttribute;


public class MKRangedAttribute extends RangedAttribute {
    private boolean additionIsPercentage;

    public MKRangedAttribute(String name, double defaultValue, double minimumValueIn, double maximumValueIn) {
        super(null, name, defaultValue, minimumValueIn, maximumValueIn);
    }

    public MKRangedAttribute setAdditionIsPercentage(boolean set) {
        additionIsPercentage = set;
        return this;
    }

    public boolean displayAdditionAsPercentage() {
        return additionIsPercentage;
    }
}
