package com.chaosbuffalo.mkcore.core;

public class PlayerFormulas {

    public static int applyCooldownReduction(IMKPlayerData playerData, int originalCooldownTicks) {
        final float MAX_COOLDOWN = 2.0f; // Maximum cooldown rate improvement is 200%
        float cdrValue = (float) playerData.getPlayer().getAttribute(PlayerAttributes.COOLDOWN).getValue();
        float mod = MAX_COOLDOWN - cdrValue;
        float newTicks = mod * originalCooldownTicks;
        return (int) newTicks;
    }
}
