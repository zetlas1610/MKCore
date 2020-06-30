package com.chaosbuffalo.mkcore.abilities.description;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.PassiveTalentAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.potion.Effect;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class AbilityDescriptions {

    public static ITextComponent getCooldownDescription(MKAbility ability, IMKEntityData entityData) {
        return new TranslationTextComponent("mkcore.ability.description.cooldown",
                String.format("%.1f seconds", (float) entityData.getStats().getAbilityCooldown(ability) / GameConstants.TICKS_PER_SECOND));
    }

    public static ITextComponent getCastTimeDescription(MKAbility ability, IMKEntityData entityData) {
        String time = ability.getCastTime(entityData) > 0 ?
                String.format("%.1f seconds", (float) ability.getCastTime(entityData) / GameConstants.TICKS_PER_SECOND)
                : I18n.format("mkcore.ability.description.instant");
        return new TranslationTextComponent("mkcore.ability.description.cast_time", time);
    }

    public static ITextComponent getManaCostDescription(MKAbility ability, IMKEntityData entityData) {
        return new TranslationTextComponent("mkcore.ability.description.mana_cost",
                Float.toString(ability.getManaCost(entityData)));
    }

    public static ITextComponent getRangeDescription(MKAbility ability, IMKEntityData entityData) {
        return new TranslationTextComponent("mkcore.ability.description.range",
                String.format("%.1f", ability.getDistance()));
    }

    public static ITextComponent getTargetTypeDescription(MKAbility ability) {
        return ability.getTargetContextLocalization();
    }

    public static ITextComponent getAbilityDescription(MKAbility ability, IMKEntityData entityData,
                                                       Function<IMKEntityData, List<Object>> argsProvider) {
        return new TranslationTextComponent(ability.getDescriptionTranslationKey(), argsProvider.apply(entityData).toArray());
    }

    public static List<ITextComponent> getEffectDescription(Effect effect, IMKEntityData entityData, boolean showName){
        List<ITextComponent> desc = new ArrayList<>();
        if (showName){
            desc.add(new TranslationTextComponent("mkcore.ability.description.effect_with_name",
                    effect.getDisplayName().getFormattedText()));
        } else {
            desc.add(new TranslationTextComponent("mkcore.ability.description.effect"));
        }
        for (Map.Entry<IAttribute, AttributeModifier> entry : effect.getAttributeModifierMap().entrySet()){
            desc.add(new StringTextComponent(String.format("    %s: %s%.2f", I18n.format(String.format("attribute.name.%s",
                    entry.getKey().getName())), entry.getValue().getAmount() > 0 ? "+" : "", entry.getValue().getAmount())));
        }
        return desc;
    }
}
