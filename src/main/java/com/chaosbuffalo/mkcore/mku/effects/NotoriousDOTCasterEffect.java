package com.chaosbuffalo.mkcore.mku.effects;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.effects.SongCasterEffect;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NotoriousDOTCasterEffect extends SongCasterEffect {
    public static final NotoriousDOTCasterEffect INSTANCE = new NotoriousDOTCasterEffect();

    public static final int PERIOD = 18 * GameConstants.TICKS_PER_SECOND;

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Effect> event) {
        event.getRegistry().register(INSTANCE);
    }

    public static SpellCast Create(LivingEntity source) {
        return INSTANCE.newSpellCast(source).setTarget(source);
    }

    private NotoriousDOTCasterEffect() {
        super(PERIOD, EffectType.NEUTRAL, 16750080);
        setRegistryName("effect.notorious_dot_song");
    }

    @Override
    protected Set<SpellCast> getSpellCasts(Entity source) {
        Set<SpellCast> ret = super.getSpellCasts(source);
        ret.add(NotoriousDOTAreaEffect.INSTANCE.newSpellCast(source));
        return ret;
    }


}
