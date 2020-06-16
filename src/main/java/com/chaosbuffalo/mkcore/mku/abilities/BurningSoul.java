package com.chaosbuffalo.mkcore.mku.abilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.PassiveTalentAbility;
import com.chaosbuffalo.mkcore.effects.PassiveTalentEffect;
import com.chaosbuffalo.mkcore.mku.effects.BurningSoulPotion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BurningSoul extends PassiveTalentAbility {

    public static final BurningSoul INSTANCE = new BurningSoul();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MKAbility> event) {
        event.getRegistry().register(INSTANCE);
    }

    public BurningSoul() {
        super(MKCore.makeRL("ability.burning_soul"));
    }

    @Override
    public PassiveTalentEffect getPassiveEffect() {
        return BurningSoulPotion.INSTANCE;
    }
}
