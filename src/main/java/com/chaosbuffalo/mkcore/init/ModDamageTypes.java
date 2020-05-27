package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.damage.DamageType;
import com.chaosbuffalo.mkcore.core.damage.MeleeDamageType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(MKCore.MOD_ID)
public class ModDamageTypes {

    @ObjectHolder("damage.shadow")
    public static DamageType ShadowDamage;

    @ObjectHolder("damage.fire")
    public static DamageType FireDamage;

    @ObjectHolder("damage.frost")
    public static DamageType FrostDamage;

    @ObjectHolder("damage.holy")
    public static DamageType HolyDamage;

    @ObjectHolder("damage.poison")
    public static DamageType PoisonDamage;

    @ObjectHolder("damage.arcane")
    public static DamageType ArcaneDamage;

    @ObjectHolder("damage.electric")
    public static DamageType ElectricDamage;

    @ObjectHolder("damage.melee")
    public static DamageType MeleeDamage;


    @SubscribeEvent
    public static void registerDamageTypes(RegistryEvent.Register<DamageType> evt) {
        evt.getRegistry().register(new DamageType(MKCore.makeRL("damage.fire"), MKAttributes.FIRE_DAMAGE,
                MKAttributes.FIRE_RESISTANCE));
        evt.getRegistry().register(new DamageType(MKCore.makeRL("damage.frost"), MKAttributes.FROST_DAMAGE,
                MKAttributes.FROST_RESISTANCE));
        evt.getRegistry().register(new DamageType(MKCore.makeRL("damage.holy"), MKAttributes.HOLY_DAMAGE,
                MKAttributes.HOLY_RESISTANCE));
        evt.getRegistry().register(new DamageType(MKCore.makeRL("damage.poison"), MKAttributes.POISON_DAMAGE,
                MKAttributes.POISON_RESISTANCE));
        evt.getRegistry().register(new DamageType(MKCore.makeRL("damage.shadow"), MKAttributes.SHADOW_DAMAGE,
                MKAttributes.SHADOW_RESISTANCE));
        evt.getRegistry().register(new DamageType(MKCore.makeRL("damage.arcane"), MKAttributes.ARCANE_DAMAGE,
                MKAttributes.ARCANE_RESISTANCE));
        evt.getRegistry().register(new DamageType(MKCore.makeRL("damage.electric"), MKAttributes.ELECTRIC_DAMAGE,
                MKAttributes.ELECTRIC_RESISTANCE));
        evt.getRegistry().register(new MeleeDamageType(MKCore.makeRL("damage.melee")));
    }
}
