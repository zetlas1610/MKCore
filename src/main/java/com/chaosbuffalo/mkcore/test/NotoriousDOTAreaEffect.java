package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.SongAreaEffect;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import com.chaosbuffalo.mkcore.init.ModSounds;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NotoriousDOTAreaEffect extends SongAreaEffect {
    public static final NotoriousDOTAreaEffect INSTANCE = new NotoriousDOTAreaEffect();
    public static final int PERIOD = 6 * GameConstants.TICKS_PER_SECOND;

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Effect> event) {
        event.getRegistry().register(INSTANCE);
    }

    public static SpellCast Create(Entity source) {
        return INSTANCE.newSpellCast(source);
    }

    @Override
    public AreaEffectBuilder prepareAreaEffect(LivingEntity source, IMKEntityData entityData,
                                               int level, AreaEffectBuilder builder) {
        SpellCast damage = AbilityMagicDamage.Create(source, NotoriousDOT.BASE_DAMAGE, NotoriousDOT.DAMAGE_SCALE, 0.6f);
        builder.spellCast(damage, level, TargetingContexts.ALL_AROUND);
        SoundUtils.playSoundAtEntity(source, ModSounds.spell_shadow_9);
        return builder;
    }

    private NotoriousDOTAreaEffect() {
        super(PERIOD, EffectType.NEUTRAL, 16750080);
        setRegistryName("effect.notorious_dot");
    }

    @Override
    public IParticleData getSongParticle() {
        return ParticleTypes.DAMAGE_INDICATOR;
    }

    @Override
    public ResourceLocation getIconTexture() {
        return MKCore.makeRL("textures/class/abilities/notorious_dot.png");
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.SELF;
    }

    @Override
    public float getSongDistance(int level) {
        return 3.0f + level * 3.0f;
    }
}
