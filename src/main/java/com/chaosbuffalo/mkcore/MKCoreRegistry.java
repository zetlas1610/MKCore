package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.core.talents.BaseTalent;
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
    public static ResourceLocation INVALID_ABILITY = new ResourceLocation(MKCore.MOD_ID, "ability.invalid");
    public static ResourceLocation INVALID_TALENT = new ResourceLocation(MKCore.MOD_ID, "talent.invalid");
    public static IForgeRegistry<MKAbility> ABILITIES = null;
    public static IForgeRegistry<MKDamageType> DAMAGE_TYPES = null;
    public static IForgeRegistry<BaseTalent> TALENT_TYPES = null;

    @Nullable
    public static MKAbility getAbility(ResourceLocation abilityId) {
        return ABILITIES.getValue(abilityId);
    }

    @Nullable
    public static MKDamageType getDamageType(ResourceLocation damageTypeId) {
        return DAMAGE_TYPES.getValue(damageTypeId);
    }

    @SubscribeEvent
    public static void createRegistries(RegistryEvent.NewRegistry event) {
        ABILITIES = new RegistryBuilder<MKAbility>()
                .setName(MKCore.makeRL("abilities"))
                .setType(MKAbility.class)
                .setIDRange(0, Integer.MAX_VALUE - 1)
                .create();
        DAMAGE_TYPES = new RegistryBuilder<MKDamageType>()
                .setName(MKCore.makeRL("damage_types"))
                .setType(MKDamageType.class)
                .setIDRange(0, Integer.MAX_VALUE - 1)
                .create();
        TALENT_TYPES = new RegistryBuilder<BaseTalent>()
                .setName(MKCore.makeRL("talent_types"))
                .setType(BaseTalent.class)
                .create();
    }
}
