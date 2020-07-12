package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.description.AbilityDescriptions;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

public class AbilityTargetSelector {

    private final BiFunction<IMKEntityData, MKAbility, AbilityContext> selector;
    private Set<MemoryModuleType<?>> requiredMemories;
    private String descriptionKey;
    private final List<BiFunction<MKAbility, IMKEntityData, ITextComponent>> additionalDescriptors;
    private boolean showTargetType;

    public AbilityTargetSelector(BiFunction<IMKEntityData, MKAbility, AbilityContext> selector) {
        this.selector = selector;
        this.additionalDescriptors = new ArrayList<>();
        this.showTargetType = true;
        descriptionKey = "";
    }

    public AbilityTargetSelector setDescriptionKey(String description) {
        this.descriptionKey = description;
        return this;
    }

    public AbilityTargetSelector setShowTargetType(boolean showTargetType) {
        this.showTargetType = showTargetType;
        return this;
    }

    public AbilityTargetSelector addDescription(BiFunction<MKAbility, IMKEntityData, ITextComponent> description) {
        additionalDescriptors.add(description);
        return this;
    }

    public boolean doShowTargetType() {
        return showTargetType;
    }

    public void fillAbilityDescription(List<ITextComponent> descriptions, MKAbility ability, IMKEntityData entityData) {
        descriptions.add(getLocalizedDescriptionForContext());
        if (doShowTargetType()) {
            descriptions.add(AbilityDescriptions.getTargetTypeDescription(ability));
        }
        additionalDescriptors.forEach(func -> {
            descriptions.add(func.apply(ability, entityData));
        });
    }

    public ITextComponent getLocalizedDescriptionForContext() {
        return new TranslationTextComponent("mkcore.ability_description.target",
                I18n.format(getDescriptionKey()));
    }

    public String getDescriptionKey() {
        return descriptionKey;
    }

    public AbilityTargetSelector setRequiredMemories(Set<MemoryModuleType<?>> types) {
        requiredMemories = types;
        return this;
    }

    public BiFunction<IMKEntityData, MKAbility, AbilityContext> getSelector() {
        return selector;
    }

    public AbilityContext createContext(IMKEntityData entityData, MKAbility ability) {
        return selector.apply(entityData, ability);
    }

    public boolean validateContext(IMKEntityData entityData, AbilityContext context) {
        MKCore.LOGGER.info("AbilityTargetSelector.validateContext {}", entityData.getEntity());
        return requiredMemories == null || requiredMemories.stream().allMatch(context::hasMemory);
    }
}
