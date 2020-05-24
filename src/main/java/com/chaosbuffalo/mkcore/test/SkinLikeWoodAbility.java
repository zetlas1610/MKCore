package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.PlayerAbility;
import com.chaosbuffalo.mkcore.abilities.PlayerToggleAbility;
import com.chaosbuffalo.mkcore.core.IMKPlayerData;
import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SkinLikeWoodAbility extends PlayerToggleAbility {
    public static final SkinLikeWoodAbility INSTANCE = new SkinLikeWoodAbility();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<PlayerAbility> event) {
        event.getRegistry().register(INSTANCE.setRegistryName(INSTANCE.getAbilityId()));
    }

    public static int BASE_DURATION = 32767;
    public static int DURATION_SCALE = 0;

    private SkinLikeWoodAbility() {
        super(MKCore.makeRL("ability.skin_like_wood"));
    }

    @Override
    public int getCooldown(int currentRank) {
        return 4 - currentRank;
    }

    @Override
    public Targeting.TargetType getTargetType() {
        return Targeting.TargetType.SELF;
    }

    @Override
    public float getManaCost(int currentRank) {
        return 3 - currentRank;
    }

    @Override
    public float getDistance(int currentRank) {
        return 1.0f;
    }

    @Override
    public Effect getToggleEffect() {
        return SkinLikeWoodPotion.INSTANCE;
    }

    @Override
    public int getRequiredLevel(int currentRank) {
        return currentRank * 2;
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return null;
    }

    @Override
    public void applyEffect(PlayerEntity entity, IMKPlayerData pData, World theWorld) {
        super.applyEffect(entity, pData, theWorld);
        int level = pData.getAbilityRank(getAbilityId());
//        AbilityUtils.playSoundAtServerEntity(entity, ModSounds.spell_earth_7, SoundCategory.PLAYERS);
        // What to do for each target hit
        entity.addPotionEffect(SkinLikeWoodPotion.Create(entity).setTarget(entity).toPotionEffect(BASE_DURATION, level));
//        Vec3d lookVec = entity.getLookVec();
//        MKUltra.packetHandler.sendToAllAround(
//                new ParticleEffectSpawnPacket(
//                        EnumParticleTypes.SLIME.getParticleID(),
//                        ParticleEffects.CIRCLE_MOTION, 30, 0,
//                        entity.posX, entity.posY + .5,
//                        entity.posZ, 1.0, 1.0, 1.0, 1.0f,
//                        lookVec),
//                entity, 50.0f);

    }
}