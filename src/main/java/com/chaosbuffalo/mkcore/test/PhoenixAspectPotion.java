package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.PassiveEffect;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PhoenixAspectPotion extends PassiveEffect {

    public static final UUID MODIFIER_ID = UUID.fromString("721f69b8-c361-4b80-897f-724f84e08ae7");

    public static final PhoenixAspectPotion INSTANCE = (PhoenixAspectPotion) new PhoenixAspectPotion()
            .addAttributesModifier(MKAttributes.COOLDOWN, MODIFIER_ID.toString(),
                    0.33, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributesModifier(MKAttributes.MANA_REGEN, MODIFIER_ID.toString(),
                    1.0f, AttributeModifier.Operation.ADDITION);

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Effect> event) {
        event.getRegistry().register(INSTANCE);
    }

    public static SpellCast Create(Entity source) {
        return INSTANCE.newSpellCast(source);
    }

    private PhoenixAspectPotion() {
        super(EffectType.BENEFICIAL, 4393423);
        setRegistryName("effect.phoenix_aspect");
    }

    @Override
    public ResourceLocation getIconTexture() {
        return MKCore.makeRL("textures/class/abilities/phoenix_aspect.png");
    }

    @Override
    public void onPotionAdd(SpellCast cast, LivingEntity target, AbstractAttributeMap attributes, int amplifier) {
        MKCore.LOGGER.info("PhoenixAspectPotion.onPotionAdd {}", target);
        if (target instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) target;
            player.abilities.allowFlying = true;
            player.sendPlayerAbilities();
        }
    }

    @Override
    public void onPotionRemove(SpellCast cast, LivingEntity target, AbstractAttributeMap attributes, int amplifier) {
        MKCore.LOGGER.info("PhoenixAspectPotion.onPotionRemove {}", target);
        if (target instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) target;
            player.abilities.allowFlying = false;
            player.abilities.isFlying = false;
            player.sendPlayerAbilities();
        }
    }
}
