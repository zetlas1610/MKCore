package com.chaosbuffalo.mkcore.core;

import net.minecraft.entity.ai.attributes.RangedAttribute;

public class PlayerAttributes {
    public static final RangedAttribute MAX_MANA = (RangedAttribute) new RangedAttribute(null, "mk.max_mana", 0, 0, 1024)
            .setDescription("Max Mana")
            .setShouldWatch(true);

    public static final RangedAttribute MANA_REGEN = (RangedAttribute) new RangedAttribute(null, "mk.mana_regen", 0, 0, 1024)
            .setDescription("Mana Regen")
            .setShouldWatch(true);

    // This is slightly confusing.
    // 1.5 max means the cooldown will progress at most 50% faster than the normal rate. This translates into a 50% reduction in the observed cooldown.
    // 0.25 minimum means that a cooldown can be increased up to 175% of the normal value. This translates into a 75% increase in the observed cooldown
    public static final RangedAttribute COOLDOWN = (RangedAttribute) new RangedAttribute(null, "mk.cooldown_rate", 1, 0.25, 1.5)
            .setDescription("Cooldown Rate")
            .setShouldWatch(true);
}
