package com.chaosbuffalo.mkcore.mku.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.effects.PassiveEffect;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SkinLikeWoodEffect extends PassiveEffect {
    public static final UUID MODIFIER_ID = UUID.fromString("60f31ee6-4a8e-4c35-8746-6c5950187e77");
    public static final SkinLikeWoodEffect INSTANCE = (SkinLikeWoodEffect) (new SkinLikeWoodEffect()
            .addAttributesModifier(SharedMonsterAttributes.ARMOR, MODIFIER_ID.toString(), 2, AttributeModifier.Operation.ADDITION)
    );

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Effect> event) {
        event.getRegistry().register(INSTANCE);
    }

    public static SpellCast Create(Entity source) {
        return INSTANCE.newSpellCast(source);
    }

    private SkinLikeWoodEffect() {
        super(EffectType.BENEFICIAL, 1665535);
        setRegistryName("effect.skin_like_wood");
        SpellTriggers.ENTITY_HURT_PLAYER.registerPreScale(this::playerHurtPreScale);
    }

    @Override
    public boolean isInfiniteDuration() {
        return true;
    }

    @Override
    public boolean shouldRender(EffectInstance effect) {
        return false;
    }

    private void playerHurtPreScale(LivingHurtEvent event, DamageSource source, PlayerEntity livingTarget, MKPlayerData targetData) {

        if (livingTarget.isPotionActive(SkinLikeWoodEffect.INSTANCE)) {
            if (!targetData.getStats().consumeMana(1)) {
                livingTarget.removePotionEffect(SkinLikeWoodEffect.INSTANCE);
            }
        }
    }
}
