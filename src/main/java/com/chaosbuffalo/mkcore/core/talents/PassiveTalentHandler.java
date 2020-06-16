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

public class PassiveTalentHandler extends TalentTypeHandler {

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
    public void onRecordUpdated(TalentRecord record) {
        if (!record.isKnown()) {
            BaseTalent talent = record.getNode().getTalent();
            if (talent instanceof PassiveTalent) {
                PassiveTalentAbility ability = ((PassiveTalent) talent).getAbility();
                playerData.getKnowledge().getTalentKnowledge().clearPassive(ability);
            }
        }
    }

    public void onSlotChanged(int index, ResourceLocation oldAbilityId, ResourceLocation newAbilityId) {
//        MKCore.LOGGER.info("PlayerTalentModule.onPassiveSlotChanged {} {} {}", index, oldAbilityId, newAbilityId);

        if (!oldAbilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
            PassiveTalentAbility current = TalentManager.getPassiveTalentAbility(oldAbilityId);
//            MKCore.LOGGER.info("current {}", current);
            if (current != null) {
//                MKCore.LOGGER.info("deactivate current {}", current);
                deactivatePassive(current);
            }
        }

        if (!newAbilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
            PassiveTalentAbility passiveTalent = TalentManager.getPassiveTalentAbility(newAbilityId);
//            MKCore.LOGGER.info("PlayerTalentModule.onPassiveSlotChanged new {} {}", newAbilityId, passiveTalent);
            if (passiveTalent != null) {
                activatePassive(passiveTalent);
            } else {
//                MKCore.LOGGER.info("PlayerTalentModule.onPassiveSlotChanged clearing slot {}", index);
            }
        }
    }

    private void activatePassive(PassiveTalentAbility talent) {
//        MKCore.LOGGER.info("PlayerTalentModule.activatePassive {}", talent);
        talent.executeWithContext(playerData, AbilityContext.selfTarget(playerData));
    }

    private void deactivatePassive(PassiveTalentAbility talent) {
//        MKCore.LOGGER.info("PlayerTalentModule.deactivatePassive {}", talent);
        removePassiveEffect(talent.getPassiveEffect());
    }

    public boolean getPassiveTalentsUnlocked() {
        return talentPassivesUnlocked;
    }

    private void activateAllPassives() {
//        MKCore.LOGGER.info("PlayerTalentModule.activateAllPassives {}", playerData.getEntity().isAddedToWorld());
        if (!playerData.getEntity().isAddedToWorld()) {
            // We come here during deserialization of the active persona, and it tries to apply effects which will crash the client because it's too early
            // Active persona passives should be caught by onJoinWorld
            // Persona switching while in-game should not go inside this branch
            return;
        }

        getPassiveAbilitiesStream().forEach(talentAbility -> {
//            MKCore.LOGGER.info("PlayerTalentModule.activateAllPassives activating {}", talentAbility);
            activatePassive(talentAbility);
        });
    }

    private void removeAllPassiveTalents() {
//        MKCore.LOGGER.info("PlayerTalentModule.removeAllPassiveTalents");
        PlayerEntity playerEntity = playerData.getEntity();

        getPassiveAbilitiesStream().forEach(talentAbility -> {
            PassiveTalentEffect talentEffect = talentAbility.getPassiveEffect();
            if (playerEntity.isPotionActive(talentEffect)) {
                removePassiveEffect(talentEffect);
            }
        });
    }

    private void removePassiveEffect(PassiveEffect passiveEffect) {
//        MKCore.LOGGER.info("PlayerTalentModule.removePassiveEffect {}", passiveEffect);
        talentPassivesUnlocked = true;
        playerData.getEntity().removePotionEffect(passiveEffect);
        talentPassivesUnlocked = false;
    }

    private Stream<PassiveTalentAbility> getPassiveAbilitiesStream() {
        return playerData.getKnowledge().getTalentKnowledge().getActivePassives()
                .stream()
                .map(TalentManager::getPassiveTalentAbility)
                .filter(Objects::nonNull);
    }
}
