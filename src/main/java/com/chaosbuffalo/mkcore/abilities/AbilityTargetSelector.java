package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.description.AbilityDescription;
import com.chaosbuffalo.mkcore.abilities.description.AbilityDescriptions;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AbilityTargetSelector {

    private final BiFunction<IMKEntityData, MKAbility, AbilityContext> selector;
    private Set<MemoryModuleType<?>> requiredMemories;
    private String descriptionKey;
    private final List<Function<MKAbility, AbilityDescription<?>>> additionalDescriptors;
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

    public AbilityTargetSelector addDescription(Function<MKAbility, AbilityDescription<?>> description){
        additionalDescriptors.add(description);
        return this;
    }

    public boolean doShowTargetType(){
        return showTargetType;
    }

    public void addDescriptionToAbilityDescriptions(List<AbilityDescription<?>> descriptions, MKAbility ability){
        descriptions.add(new AbilityDescription<>(ability, ((mkAbility, entityData) ->
                this.getLocalizedDescriptionForContext())));
        if (doShowTargetType()){
            descriptions.add(AbilityDescriptions.getTargetTypeDescription(ability));
        }
        descriptions.addAll(
                additionalDescriptors
                .stream()
                .map(factory -> factory.apply(ability))
                .collect(Collectors.toList())
        );
    }

    public ITextComponent getLocalizedDescriptionForContext(){
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
