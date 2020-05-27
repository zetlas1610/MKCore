package com.chaosbuffalo.mkcore.core;

import net.minecraft.entity.ai.attributes.RangedAttribute;

public class MKAttributes {
    // Players Only Attributes
    public static final RangedAttribute MAX_MANA = (RangedAttribute) new RangedAttribute(null, "mk.max_mana", 0, 0, 1024)
            .setDescription("Max Mana")
            .setShouldWatch(true);

    public static final RangedAttribute MANA_REGEN = (RangedAttribute) new RangedAttribute(null, "mk.mana_regen", 0, 0, 1024)
            .setDescription("Mana Regen")
            .setShouldWatch(true);

    public static final RangedAttribute MELEE_CRIT = (RangedAttribute) new RangedAttribute(null, "mk.melee_crit_chance", 0.00, 0.0, 1.0)
            .setDescription("Melee Critical Chance")
            .setShouldWatch(true);

    public static final RangedAttribute SPELL_CRIT = (RangedAttribute) new RangedAttribute(null, "mk.spell_crit_chance", 0.1, 0.0, 1.0)
            .setDescription("Spell Critical Chance")
            .setShouldWatch(true);

    public static final RangedAttribute SPELL_CRITICAL_DAMAGE = (RangedAttribute) new RangedAttribute(null, "mk.spell_crit_damage", 1.5, 0.0, 10.0)
            .setDescription("Spell Critical Damage")
            .setShouldWatch(true);

    public static final RangedAttribute MELEE_CRITICAL_DAMAGE = (RangedAttribute) new RangedAttribute(null, "mk.melee_crit_damage", 0.0, 0.0, 10.0)
            .setDescription("Melee Critical Damage")
            .setShouldWatch(true);

    // Everyone Attributes

    // This is slightly confusing.
    // 1.5 max means the cooldown will progress at most 50% faster than the normal rate. This translates into a 50% reduction in the observed cooldown.
    // 0.25 minimum means that a cooldown can be increased up to 175% of the normal value. This translates into a 75% increase in the observed cooldown
    public static final RangedAttribute COOLDOWN = (RangedAttribute) new RangedAttribute(null, "mk.cooldown_rate", 1, 0.25, 1.5)
            .setDescription("Cooldown Rate")
            .setShouldWatch(true);

    public static final RangedAttribute HEAL_BONUS = (RangedAttribute) new RangedAttribute(null, "mk.heal_bonus", 1.0, 0.0, 2.0)
            .setDescription("Heal Bonus Amount")
            .setShouldWatch(true);

    public static final RangedAttribute ARCANE_RESISTANCE = (RangedAttribute) new RangedAttribute(null, "mk.arcane_resistance", 0, -1.0, 1.0)
            .setDescription("Arcane Resistance")
            .setShouldWatch(true);

    public static final RangedAttribute ARCANE_DAMAGE = (RangedAttribute) new RangedAttribute(null, "mk.arcane_damage", 0, 0, 2048)
            .setDescription("Arcane Damage")
            .setShouldWatch(true);

    public static final RangedAttribute FIRE_RESISTANCE = (RangedAttribute) new RangedAttribute(null, "mk.fire_resistance", 0, -1.0, 1.0)
            .setDescription("Fire Resistance")
            .setShouldWatch(true);

    public static final RangedAttribute FIRE_DAMAGE = (RangedAttribute) new RangedAttribute(null, "mk.fire_damage", 0, 0, 2048)
            .setDescription("Fire Damage")
            .setShouldWatch(true);

    public static final RangedAttribute FROST_RESISTANCE = (RangedAttribute) new RangedAttribute(null, "mk.frost_resistance", 0, -1.0, 1.0)
            .setDescription("Frost Resistance")
            .setShouldWatch(true);

    public static final RangedAttribute FROST_DAMAGE = (RangedAttribute) new RangedAttribute(null, "mk.frost_damage", 0, 0, 2048)
            .setDescription("Frost Damage")
            .setShouldWatch(true);

    public static final RangedAttribute SHADOW_RESISTANCE = (RangedAttribute) new RangedAttribute(null, "mk.shadow_resistance", 0, -1.0, 1.0)
            .setDescription("Shadow Resistance")
            .setShouldWatch(true);

    public static final RangedAttribute SHADOW_DAMAGE = (RangedAttribute) new RangedAttribute(null, "mk.shadow_damage", 0, 0, 2048)
            .setDescription("Shadow Damage")
            .setShouldWatch(true);

    public static final RangedAttribute HOLY_RESISTANCE = (RangedAttribute) new RangedAttribute(null, "mk.holy_resistance", 0, -1.0, 1.0)
            .setDescription("Holy Resistance")
            .setShouldWatch(true);

    public static final RangedAttribute HOLY_DAMAGE = (RangedAttribute) new RangedAttribute(null, "mk.holy_damage", 0, 0, 2048)
            .setDescription("Holy Damage")
            .setShouldWatch(true);

    public static final RangedAttribute ELECTRIC_RESISTANCE = (RangedAttribute) new RangedAttribute(null, "mk.electric_resistance", 0, -1.0, 1.0)
            .setDescription("Electric Resistance")
            .setShouldWatch(true);

    public static final RangedAttribute ELECTRIC_DAMAGE = (RangedAttribute) new RangedAttribute(null, "mk.electric_damage", 0, 0, 2048)
            .setDescription("Electric Damage")
            .setShouldWatch(true);

    public static final RangedAttribute POISON_RESISTANCE = (RangedAttribute) new RangedAttribute(null, "mk.poison_resistance", 0, -1.0, 1.0)
            .setDescription("Poison Resistance")
            .setShouldWatch(true);

    public static final RangedAttribute POISON_DAMAGE = (RangedAttribute) new RangedAttribute(null, "mk.poison_damage", 0, 0, 2048)
            .setDescription("Poison Damage")
            .setShouldWatch(true);
}
