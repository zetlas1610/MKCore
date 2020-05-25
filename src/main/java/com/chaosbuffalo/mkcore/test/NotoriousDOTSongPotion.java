package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.effects.SongApplicator;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import net.minecraft.entity.Entity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NotoriousDOTSongPotion extends SongApplicator {
    public static final NotoriousDOTSongPotion INSTANCE = new NotoriousDOTSongPotion();

    public static final int PERIOD = 18 * GameConstants.TICKS_PER_SECOND;

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Effect> event) {
        event.getRegistry().register(INSTANCE.finish());
    }

    public static SpellCast Create(Entity source) {
        return INSTANCE.newSpellCast(source);
    }

    private NotoriousDOTSongPotion() {
        super(PERIOD, EffectType.NEUTRAL, 16750080);
        setRegistryName("effect.notorious_dot_song");
    }

    @Override
    public Set<SpellCast> getSpellCasts(Entity source) {
        Set<SpellCast> ret = super.getSpellCasts(source);
        ret.add(NotoriousDOTPotion.Create(source));
        return ret;
    }


    @Override
    public ResourceLocation getIconTexture() {
        return MKCore.makeRL("textures/class/abilities/notorious_dot.png");
    }

}
