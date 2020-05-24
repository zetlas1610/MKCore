package com.chaosbuffalo.mkcore.effects;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpellManager {

    private static final Map<UUID, Map<SpellPotionBase, SpellCast>> allCasts = new HashMap<>(new HashMap<>());

    public static SpellCast create(SpellPotionBase potion, Entity caster) {
        return new SpellCast(potion, caster);
    }

    public static SpellCast get(LivingEntity target, SpellPotionBase potion) {

        Map<SpellPotionBase, SpellCast> targetSpells = allCasts.get(target.getUniqueID());
        if (targetSpells == null) {
//            Log.warn("Tried to get a spell on an unregistered target! Spell: %s", potion.getName());
            return null;
        }

        SpellCast cast = targetSpells.get(potion);
        if (cast != null) {
            cast.updateRefs(target.world);
        }
        return cast;
    }

    public static void registerTarget(SpellCast cast, LivingEntity target) {
        allCasts.computeIfAbsent(target.getUniqueID(), k -> new HashMap<>()).put(cast.getPotion(), cast);
    }
}
