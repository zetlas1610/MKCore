package com.chaosbuffalo.mkcore;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class MKConfig {

    private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec SERVER_CONFIG;
    public static final ForgeConfigSpec CLIENT_CONFIG;

    static {
        initClient(CLIENT_BUILDER);
        initServer(SERVER_BUILDER);

        SERVER_CONFIG = SERVER_BUILDER.build();
        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

    public static ForgeConfigSpec.IntValue talentPointLimit;

    public static ForgeConfigSpec.BooleanValue showMyCrits;
    public static ForgeConfigSpec.BooleanValue showOthersCrits;
    public static ForgeConfigSpec.BooleanValue enablePlayerCastAnimations;

    public static ForgeConfigSpec.BooleanValue healsDamageUndead;
    public static ForgeConfigSpec.ConfigValue<Double> undeadHealDamageMultiplier;

    private static void initClient(ForgeConfigSpec.Builder builder) {
        builder.comment("General settings").push("general");
        showMyCrits = builder
                .comment("Show your own crit messages")
                .define("showMyCrits", true);
        showOthersCrits = builder
                .comment("Show other's crit messages")
                .define("showOthersCrits", true);
        enablePlayerCastAnimations = builder
                .comment("Enable player cast animations. Requires client restart to take effect")
                .worldRestart()
                .define("enablePlayerCastAnimations", true);
        builder.pop();
    }

    private static void initServer(ForgeConfigSpec.Builder builder) {
        builder.comment("General settings").push("general");
        builder.pop();

        builder.comment("Gameplay settings").push("gameplay");
        talentPointLimit = builder
                .comment("Max number of talents (-1 for unlimited)")
                .defineInRange("talentPointLimit", -1, -1, Integer.MAX_VALUE);
        healsDamageUndead = builder
                .comment("Should healing spells damage undead entities")
                .define("healsDamageUndead", true);
        undeadHealDamageMultiplier = builder
                .comment("Damage multiplier to use when healing spells damage undead entities (if healsDamageUndead is set)")
                .define("undeadHealDamageMultiplier", 2.0);
        builder.pop();
    }
}
