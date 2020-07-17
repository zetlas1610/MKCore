package com.chaosbuffalo.mkcore.core;

public enum AbilitySlot {
    Basic(true),
    Passive(false),
    Ultimate(true),
    Item(true);

    final boolean executable;

    AbilitySlot(boolean executable) {
        this.executable = executable;
    }

    public boolean isExecutable() {
        return executable;
    }
}
