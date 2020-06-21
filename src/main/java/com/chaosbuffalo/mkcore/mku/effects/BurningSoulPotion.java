package com.chaosbuffalo.mkcore.mku.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.PassiveTalentEffect;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID)
public class BurningSoulPotion extends PassiveTalentEffect {

    public static final UUID MODIFIER_ID = UUID.fromString("1f7540cb-a9c3-4a11-866d-24547723dd06");

    public static final BurningSoulPotion INSTANCE = (BurningSoulPotion) (new BurningSoulPotion()
            .addAttributesModifier(MKAttributes.SPELL_CRIT_MULTIPLIER, MODIFIER_ID.toString(), 1.0, AttributeModifier.Operation.ADDITION)
            .addAttributesModifier(MKAttributes.SPELL_CRIT, MODIFIER_ID.toString(), 0.1, AttributeModifier.Operation.ADDITION)
    );

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Effect> event) {
        event.getRegistry().register(INSTANCE);
    }

    private BurningSoulPotion() {
        super();
        setRegistryName("effect.burning_soul");
    }

    @Override
    public ResourceLocation getIconTexture() {
        return new ResourceLocation(MKCore.MOD_ID, "textures/class/abilities/burning_soul.png");
    }
}
