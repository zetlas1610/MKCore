package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.PassiveTalentAbility;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.effects.PassiveEffect;
import com.chaosbuffalo.mkcore.effects.PassiveTalentEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;
import java.util.stream.Stream;

public class PassiveTalentHandler extends AbilityTalentHandler {

    private boolean talentPassivesUnlocked;

    public PassiveTalentHandler(MKPlayerData playerData) {
        super(playerData);
    }

    @Override
    public void onJoinWorld() {
        if (playerData.isServerSide()) {
            activateAllPassives();
        }
    }

    @Override
    public void onPersonaActivated() {
        activateAllPassives();
    }

    @Override
    public void onPersonaDeactivated() {
        removeAllPassiveTalents();
    }

    @Override
    public void onSlotChanged(int index, ResourceLocation oldAbilityId, ResourceLocation newAbilityId) {
        if (!oldAbilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
            PassiveTalentAbility current = TalentManager.getPassiveTalentAbility(oldAbilityId);
            if (current != null) {
                deactivatePassive(current);
            }
        }

        if (!newAbilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
            PassiveTalentAbility passiveTalent = TalentManager.getPassiveTalentAbility(newAbilityId);
            if (passiveTalent != null) {
                activatePassive(passiveTalent);
            }
        }
    }

    private void activatePassive(PassiveTalentAbility talentAbility) {
        talentAbility.executeWithContext(playerData, AbilityContext.selfTarget(playerData));
    }

    private void deactivatePassive(PassiveTalentAbility talent) {
        removePassiveEffect(talent.getPassiveEffect());
    }

    private Stream<PassiveTalentAbility> getPassiveAbilitiesStream() {
        return playerData.getKnowledge().getTalentKnowledge()
                .getPassiveContainer()
                .getAbilities()
                .stream()
                .map(TalentManager::getPassiveTalentAbility)
                .filter(Objects::nonNull);
    }

    private void activateAllPassives() {
        if (!playerData.getEntity().isAddedToWorld()) {
            // We come here during deserialization of the active persona, and it tries to apply effects which will crash the client because it's too early
            // Active persona passives should be caught by onJoinWorld
            // Persona switching while in-game should not go inside this branch
            return;
        }

        getPassiveAbilitiesStream().forEach(this::activatePassive);
    }

    private void removeAllPassiveTalents() {
        PlayerEntity playerEntity = playerData.getEntity();

        getPassiveAbilitiesStream().forEach(talentAbility -> {
            PassiveTalentEffect talentEffect = talentAbility.getPassiveEffect();
            if (playerEntity.isPotionActive(talentEffect)) {
                removePassiveEffect(talentEffect);
            }
        });
    }

    public boolean getPassiveTalentsUnlocked() {
        return talentPassivesUnlocked;
    }

    private void removePassiveEffect(PassiveEffect passiveEffect) {
        talentPassivesUnlocked = true;
        playerData.getEntity().removePotionEffect(passiveEffect);
        talentPassivesUnlocked = false;
    }
}
