package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.effects.PassiveEffect;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FeatherFallEffect extends PassiveEffect {

    public static final FeatherFallEffect INSTANCE = new FeatherFallEffect();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Effect> event) {
        event.getRegistry().register(INSTANCE);
    }

    public static SpellCast Create(Entity source) {
        return INSTANCE.newSpellCast(source);
    }


    private FeatherFallEffect() {
        super(EffectType.BENEFICIAL, 16750080);
        setRegistryName("effect.featherfall");
        SpellTriggers.FALL.register(this::onFall);
    }


    private void onFall(LivingHurtEvent event, DamageSource source, LivingEntity entity) {
        if (entity.isPotionActive(INSTANCE)) {
            event.setAmount(0.0f);
            if (entity instanceof PlayerEntity) {
                entity.sendMessage(new StringTextComponent("My legs are OK"));
            }
        }
    }

    @Override
    protected boolean shouldShowParticles() {
        return false;
    }
}
