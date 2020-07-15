package com.chaosbuffalo.mkcore;

public class GameConstants {
    public static final int MAX_PASSIVES = 3;
    public static final int PASSIVE_INVALID_SLOT = -1;
    public static final int MAX_ULTIMATES = 2;
    public static final int ULTIMATE_INVALID_SLOT = -1;
    public static final int MAX_ACTIVES = 5;
    public static final int DEFAULT_ACTIVES = 4;
    public static final int DEFAULT_ULTIMATES = 1;
    public static final int DEFAULT_PASSIVES = 1;

    public static final int ACTION_BAR_SIZE = MAX_ACTIVES + MAX_ULTIMATES;
    public static final int ACTION_BAR_INVALID_COOLDOWN = -1;
    public static final int ACTION_BAR_INVALID_SLOT = -1;

    public static final int TICKS_PER_SECOND = 20;

    public static final int GLOBAL_COOLDOWN_TICKS = 1 * TICKS_PER_SECOND;
    public static final int DEFAULT_ABILITY_POOL_SIZE = 8;
    public static final int MAX_ABILITY_POOL_SIZE = 20;
}
