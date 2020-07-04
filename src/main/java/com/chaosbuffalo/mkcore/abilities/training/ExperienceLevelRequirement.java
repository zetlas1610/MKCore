package com.chaosbuffalo.mkcore.abilities.training;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ExperienceLevelRequirement implements IAbilityLearnRequirement {
    private final int requiredLevel;

    public ExperienceLevelRequirement(int reqLevel) {
        requiredLevel = reqLevel;
    }

    @Override
    public boolean check(IMKEntityData entityData, MKAbility ability) {
        if (entityData.getEntity() instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity)entityData.getEntity();
            return playerEntity.experienceLevel >= requiredLevel;
        }
        return false;
    }

    @Override
    public void onLearned(IMKEntityData entityData, MKAbility ability) {
        if (entityData.getEntity() instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity) entityData.getEntity();
            playerEntity.addExperienceLevel(-requiredLevel);
        }
    }

    @Override
    public ITextComponent describe() {
        return new StringTextComponent(String.format("You must be at least level %d", requiredLevel));
    }
}
