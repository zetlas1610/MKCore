package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.talents.AttributeTalent;
import com.chaosbuffalo.mkcore.core.talents.BaseTalent;
import com.chaosbuffalo.mkcore.core.talents.PassiveTalent;
import com.chaosbuffalo.mkcore.core.talents.UltimateTalent;
import com.chaosbuffalo.mkcore.mku.abilities.BurningSoul;
import com.chaosbuffalo.mkcore.mku.abilities.HealingRain;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
//@ObjectHolder(MKCore.MOD_ID)
public class ModTalents {

    private static void registerVanillaAttributeTalents(RegistryEvent.Register<BaseTalent> event) {
        // Vanilla Attributes
        AttributeTalent maxHealth = new AttributeTalent(
                MKCore.makeRL("talent.max_health"),
                (RangedAttribute) SharedMonsterAttributes.MAX_HEALTH,
                UUID.fromString("5d95bcd4-a06e-415a-add0-f1f85e20b18b"))
                .setDefaultPerRank(1);
        event.getRegistry().register(maxHealth);

        AttributeTalent armor = new AttributeTalent(
                MKCore.makeRL("talent.armor"),
                (RangedAttribute) SharedMonsterAttributes.ARMOR,
                UUID.fromString("1f917d51-efa1-43ee-8af0-b49175c97c0b"))
                .setDefaultPerRank(1);
        event.getRegistry().register(armor);

        AttributeTalent movementSpeed = new AttributeTalent(
                MKCore.makeRL("talent.movement_speed"),
                (RangedAttribute) SharedMonsterAttributes.MOVEMENT_SPEED,
                UUID.fromString("95fcf4d0-aaa9-413f-8362-7706e29412f7"))
                .setDisplayAsPercentage(true);
        event.getRegistry().register(movementSpeed);
    }

    @SubscribeEvent
    public static void registerTalents(RegistryEvent.Register<BaseTalent> event) {
        registerVanillaAttributeTalents(event);

        // MKCore
        registerMKAttributeTalents(event);
        registerPassiveTalents(event);
        registerUltimateTalents(event);
    }

    private static void registerPassiveTalents(RegistryEvent.Register<BaseTalent> event) {
        PassiveTalent burningSoul = new PassiveTalent(
                MKCore.makeRL("talent.burning_soul"),
                BurningSoul.INSTANCE);
        event.getRegistry().register(burningSoul);
    }

    private static void registerUltimateTalents(RegistryEvent.Register<BaseTalent> event) {
        UltimateTalent healingRain = new UltimateTalent(
                MKCore.makeRL("talent.healing_rain"),
                HealingRain.INSTANCE);
        event.getRegistry().register(healingRain);
    }

    private static void registerMKAttributeTalents(RegistryEvent.Register<BaseTalent> event) {
        AttributeTalent maxMana = new AttributeTalent(
                MKCore.makeRL("talent.max_mana"),
                MKAttributes.MAX_MANA,
                UUID.fromString("50338dba-eaca-4ec8-a71f-13b5924496f4"))
                .setDefaultPerRank(1);
        event.getRegistry().register(maxMana);


        AttributeTalent manaRegen = new AttributeTalent(
                MKCore.makeRL("talent.mana_regen"),
                MKAttributes.MANA_REGEN,
                UUID.fromString("87cd1a11-682f-4635-97db-4fedf6a7496b"))
                .setDefaultPerRank(0.5f);
        event.getRegistry().register(manaRegen);

        AttributeTalent meleeCrit = new AttributeTalent(
                MKCore.makeRL("talent.melee_crit"),
                MKAttributes.MELEE_CRIT,
                UUID.fromString("3b9ea27d-61ca-47b4-9bba-e82679b74ddd"))
                .setDisplayAsPercentage(true);
        event.getRegistry().register(meleeCrit);

        AttributeTalent spellCrit = new AttributeTalent(
                MKCore.makeRL("talent.spell_crit"),
                MKAttributes.SPELL_CRIT,
                UUID.fromString("9fbc7b94-4836-45ca-933a-4edaabcf2c6a"))
                .setDisplayAsPercentage(true);
        event.getRegistry().register(spellCrit);

        AttributeTalent meleeCritDamage = new AttributeTalent(
                MKCore.makeRL("talent.melee_crit_multiplier"),
                MKAttributes.MELEE_CRIT_MULTIPLIER,
                UUID.fromString("0032d49a-ed71-4dfb-a9f5-f0d3dd183e96"))
                .setDisplayAsPercentage(true);
        event.getRegistry().register(meleeCritDamage);

        AttributeTalent spellCritDamage = new AttributeTalent(
                MKCore.makeRL("talent.spell_crit_multiplier"),
                MKAttributes.SPELL_CRIT_MULTIPLIER,
                UUID.fromString("a9d6069c-98b9-454d-b59f-c5a6e81966d5"))
                .setDisplayAsPercentage(true);
        event.getRegistry().register(spellCritDamage);

        AttributeTalent cooldownRate = new AttributeTalent(
                MKCore.makeRL("talent.cooldown_reduction"),
                MKAttributes.COOLDOWN,
                UUID.fromString("5378ff4c-0606-4781-abc0-c7d3e945b378"))
                .setOp(AttributeModifier.Operation.MULTIPLY_TOTAL)
                .setDisplayAsPercentage(true);
        event.getRegistry().register(cooldownRate);

        AttributeTalent healBonus = new AttributeTalent(
                MKCore.makeRL("talent.heal_bonus"),
                MKAttributes.HEAL_BONUS,
                UUID.fromString("711e57c3-cf2a-4fb5-a503-3dff0a1e007d"));
        event.getRegistry().register(healBonus);
    }

}
