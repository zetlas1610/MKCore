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

    private static void initClient(ForgeConfigSpec.Builder builder) {
        builder.comment("General settings").push("general");
        showMyCrits = builder
                .comment("Show your own crit messages")
                .define("showMyCrits", true);
        showOthersCrits = builder
                .comment("Show other's crit messages")
                .define("showOthersCrits", true);
        builder.pop();
    }

    private static void initServer(ForgeConfigSpec.Builder builder) {
        builder.comment("General settings").push("general");
        builder.pop();

        builder.comment("Gameplay settings").push("gameplay");
        talentPointLimit = builder
                .comment("Max number of talents (-1 for unlimited)")
                .defineInRange("talentPointLimit", -1, -1, Integer.MAX_VALUE);
        builder.pop();
    }
}
