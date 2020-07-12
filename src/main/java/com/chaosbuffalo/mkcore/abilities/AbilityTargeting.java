package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.description.AbilityDescriptions;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.LivingEntity;

public class AbilityTargeting {

    public static AbilityTargetSelector NONE = new AbilityTargetSelector((entityData, ability) -> AbilityContext.EMPTY)
            .setDescriptionKey("mkcore.ability_target.none");

    public static AbilityTargetSelector SELF = new AbilityTargetSelector(AbilityTargeting::selectSelf)
            .setRequiredMemories(ImmutableSet.of(MKAbilityMemories.ABILITY_TARGET))
            .setShowTargetType(false)
            .setDescriptionKey("mkcore.ability_target.self");

    public static AbilityTargetSelector SINGLE_TARGET = new AbilityTargetSelector(AbilityTargeting::selectSingle)
            .setRequiredMemories(ImmutableSet.of(MKAbilityMemories.ABILITY_TARGET))
            .addDescription(AbilityDescriptions::getRangeDescription)
            .setDescriptionKey("mkcore.ability_target.single_target");

    public static AbilityTargetSelector SINGLE_TARGET_OR_SELF = new AbilityTargetSelector(AbilityTargeting::selectSingleOrSelf)
            .setRequiredMemories(ImmutableSet.of(MKAbilityMemories.ABILITY_TARGET))
            .addDescription(AbilityDescriptions::getRangeDescription)
            .setDescriptionKey("mkcore.ability_target.single_target_self");

    public static AbilityTargetSelector PBAOE = new AbilityTargetSelector((entityData, mkAbility) -> AbilityContext.EMPTY)
            .setDescriptionKey("mkcore.ability_target.pbaoe")
            .addDescription(AbilityDescriptions::getRangeDescription);


    private static AbilityContext selectSelf(IMKEntityData entityData, MKAbility ability) {
        MKCore.LOGGER.info("AbilityTargeting.SELF {} {}", ability.getAbilityId(), entityData.getEntity());
        return AbilityContext.selfTarget(entityData);
    }

    private static AbilityContext selectSingle(IMKEntityData entityData, MKAbility ability) {
        LivingEntity targetEntity = ability.getSingleLivingTarget(entityData.getEntity(), ability.getDistance());
        MKCore.LOGGER.info("AbilityTargeting.SINGLE_TARGET {} {} {}", ability.getAbilityId(), entityData.getEntity(), targetEntity);
        return AbilityContext.singleTarget(targetEntity);
    }

    private static AbilityContext selectSingleOrSelf(IMKEntityData entityData, MKAbility ability) {
        LivingEntity targetEntity = ability.getSingleLivingTargetOrSelf(entityData.getEntity(), ability.getDistance(), true);
        MKCore.LOGGER.info("AbilityTargeting.SINGLE_TARGET_OR_SELF {} {} {}", ability.getAbilityId(), entityData.getEntity(), targetEntity);
        return AbilityContext.singleTarget(targetEntity);
    }
}
