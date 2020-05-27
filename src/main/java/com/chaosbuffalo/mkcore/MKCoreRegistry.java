package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.abilities.PlayerAbility;
import com.chaosbuffalo.mkcore.core.damage.DamageType;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKCoreRegistry {
    public static ResourceLocation INVALID_CLASS = new ResourceLocation(MKCore.MOD_ID, "class.invalid");
    public static ResourceLocation INVALID_ABILITY = new ResourceLocation(MKCore.MOD_ID, "ability.invalid");
    public static IForgeRegistry<PlayerAbility> ABILITIES = null;
    public static IForgeRegistry<DamageType> DAMAGE_TYPES = null;


    @Nullable
    public static PlayerAbility getAbility(ResourceLocation abilityId) {
        return ABILITIES.getValue(abilityId);
    }

    @Nullable
    public static DamageType getDamageType(ResourceLocation damageTypeId) { return DAMAGE_TYPES.getValue(damageTypeId); }

    @SubscribeEvent
    public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
        // register a new block here
        MKCore.LOGGER.info("HELLO from Register Block");
    }

    @SubscribeEvent
    public static void createRegistries(RegistryEvent.NewRegistry event) {
        ABILITIES = new RegistryBuilder<PlayerAbility>()
                .setName(MKCore.makeRL("abilities"))
                .setType(PlayerAbility.class)
                .setIDRange(0, Integer.MAX_VALUE - 1)
                .create();
        DAMAGE_TYPES = new RegistryBuilder<DamageType>()
                .setName(MKCore.makeRL("damage_types"))
                .setType(DamageType.class)
                .setIDRange(0, Integer.MAX_VALUE -1)
                .create();
    }
}
