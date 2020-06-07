package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.CastState;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.MKToggleAbility;
import com.chaosbuffalo.mkcore.events.PlayerAbilityEvent;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

public class PlayerAbilityExecutor extends AbilityExecutor {

    public PlayerAbilityExecutor(MKPlayerData playerData) {
        super(playerData);
    }

    private MKPlayerData getPlayerData() {
        return (MKPlayerData) entityData;
    }

    private boolean isServerSide() {
        return getPlayerData().getEntity() instanceof ServerPlayerEntity;
    }

    public void executeHotBarAbility(int slot) {
        ResourceLocation abilityId = getPlayerData().getKnowledge().getActionBar().getAbilityInSlot(slot);
        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY))
            return;

        executeAbility(abilityId);
    }

    @Override
    protected void consumeResource(MKAbility ability) {
        float manaCost = getPlayerData().getStats().getAbilityManaCost(ability);
        getPlayerData().getStats().consumeMana(manaCost);
    }

    @Override
    protected boolean abilityExecutionCheck(MKAbility ability, MKAbilityInfo info) {
        return super.abilityExecutionCheck(ability, info) &&
                !MinecraftForge.EVENT_BUS.post(new PlayerAbilityEvent.StartCasting(getPlayerData(), info));
    }

    @Override
    protected void completeAbility(MKAbility ability, MKAbilityInfo info, CastState castState) {
        super.completeAbility(ability, info, castState);
        MinecraftForge.EVENT_BUS.post(new PlayerAbilityEvent.Completed(getPlayerData(), info));
    }

    public void onPersonaActivated() {
        rebuildActiveToggleMap();
    }

    public void onPersonaDeactivated() {
        deactivateCurrentToggleAbilities();
    }

    public float getCurrentAbilityCooldownPercent(ResourceLocation abilityId, float partialTicks) {
        return getPlayerData().getStats().getTimerPercent(abilityId, partialTicks);
    }

    private void deactivateCurrentToggleAbilities() {
        PlayerActionBar actionBar = getPlayerData().getKnowledge().getActionBar();
        for (int i = 0; i < GameConstants.ACTION_BAR_SIZE; i++) {
            ResourceLocation abilityId = actionBar.getAbilityInSlot(i);
            MKAbility ability = MKCoreRegistry.getAbility(abilityId);
            if (ability instanceof MKToggleAbility && entityData.getEntity() != null) {
                MKToggleAbility toggle = (MKToggleAbility) ability;
                toggle.removeEffect(entityData.getEntity(), entityData);
            }
        }
    }

    private void rebuildActiveToggleMap() {
        // Inspect the player's action bar and see if there are any toggle abilities slotted.
        // If there are, and the corresponding toggle effect is active on the player, set the toggle exclusive group
        for (int i = 0; i < GameConstants.ACTION_BAR_SIZE; i++) {
            ResourceLocation abilityId = getPlayerData().getKnowledge().getActionBar().getAbilityInSlot(i);
            MKAbility ability = MKCoreRegistry.getAbility(abilityId);
            if (ability instanceof MKToggleAbility && entityData.getEntity() != null) {
                MKToggleAbility toggle = (MKToggleAbility) ability;
                if (entityData.getEntity().isPotionActive(toggle.getToggleEffect()))
                    setToggleGroupAbility(toggle.getToggleGroupId(), toggle);
            }
        }
    }
}
