package com.chaosbuffalo.mkcore.core;

public enum AbilitySlotType {
    Basic(true),
    Passive(false),
    Ultimate(true),
    Item(true);

    final boolean executable;

    AbilitySlotType(boolean executable) {
        this.executable = executable;
    }

    public boolean isExecutable() {
        return executable;
    }
}
