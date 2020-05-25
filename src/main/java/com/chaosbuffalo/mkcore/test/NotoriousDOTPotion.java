package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKPlayerData;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.SongEffect;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NotoriousDOTPotion extends SongEffect {
    public static final NotoriousDOTPotion INSTANCE = new NotoriousDOTPotion();
    public static final int PERIOD = 6 * GameConstants.TICKS_PER_SECOND;

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Effect> event) {
        event.getRegistry().register(INSTANCE.finish());
    }

    public static SpellCast Create(Entity source) {
        return INSTANCE.newSpellCast(source);
    }

    @Override
    public AreaEffectBuilder prepareAreaEffect(PlayerEntity source, IMKPlayerData playerData, int level, AreaEffectBuilder builder) {
        builder.spellCast(AbilityMagicDamage.Create(source, NotoriousDOT.BASE_DAMAGE, NotoriousDOT.DAMAGE_SCALE, 0.6f),
                level, Targeting.TargetType.ALL, true);
//        builder.effect(DamageSource.causeIndirectMagicDamage(source, source))
//        AbilityUtils.playSoundAtServerEntity(source, ModSounds.spell_shadow_9, SoundCategory.PLAYERS);
        return builder;
    }

    private NotoriousDOTPotion() {
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
    public Targeting.TargetType getTargetType() {
        return Targeting.TargetType.SELF;
    }

    @Override
    public float getSongDistance(int level) {
        return 3.0f + level * 3.0f;
    }
}
