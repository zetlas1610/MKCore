package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.CastState;
import com.chaosbuffalo.mkcore.abilities.PlayerAbility;
import com.chaosbuffalo.mkcore.core.IMKPlayerData;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.ParticlePotion;
import com.chaosbuffalo.mkcore.effects.SpellCast;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WhirlwindBlades extends PlayerAbility {
    public static WhirlwindBlades INSTANCE = new WhirlwindBlades();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<PlayerAbility> event) {
        event.getRegistry().register(INSTANCE.setRegistryName(INSTANCE.getAbilityId()));
    }

    public static float BASE_DAMAGE = 2.0f;
    public static float DAMAGE_SCALE = 1.0f;

    private WhirlwindBlades() {
        super(MKCore.makeRL("ability.whirlwind_blades"));
    }

    @Override
    public int getCooldown(int currentRank) {
        return 25 - 5 * currentRank;
    }

    @Override
    public Targeting.TargetType getTargetType() {
        return Targeting.TargetType.ENEMY;
    }

    @Override
    public float getManaCost(int currentRank) {
        return 4 + 2 * currentRank;
    }

    @Override
    public float getDistance(int currentRank) {
        return 3.0f + currentRank * 1.0f;
    }

    @Override
    public int getRequiredLevel(int currentRank) {
        return 6 + currentRank * 2;
    }

//    @Override
//    public SoundEvent getCastingSoundEvent() {
//        return ModSounds.spell_whirlwind_1;
//    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return null;
    }

    @Override
    public int getCastTime(int currentRank) {
        return currentRank * GameConstants.TICKS_PER_SECOND * 3;
    }

    @Override
    public void continueCast(PlayerEntity entity, IMKPlayerData data, World theWorld, int castTimeLeft, CastState state) {
        super.continueCast(entity, data, theWorld, castTimeLeft, state);
        int tickSpeed = 6;
        if (castTimeLeft % tickSpeed == 0) {
            int level = data.getAbilityRank(getAbilityId());
            int totalDuration = getCastTime(level);
            int count = (totalDuration - castTimeLeft) / tickSpeed;
            float baseAmount = level > 1 ? 0.10f : 0.15f;
            float scaling = count * baseAmount;
            // What to do for each target hit
            SpellCast damage = AbilityMagicDamage.Create(entity, BASE_DAMAGE, DAMAGE_SCALE, scaling);
            SpellCast particlePotion = ParticlePotion.Create(entity,
                    ParticleTypes.SWEEP_ATTACK,
                    ParticleEffects.CIRCLE_MOTION, false,
                    new Vec3d(1.0, 1.0, 1.0),
                    new Vec3d(0.0, 1.0, 0.0),
                    4, 0, 1.0);


            AreaEffectBuilder.Create(entity, entity)
                    .spellCast(damage, level, getTargetType())
                    .spellCast(particlePotion, level, getTargetType())
//                    .spellCast(SoundPotion.Create(entity, ModSounds.spell_shadow_2, SoundCategory.PLAYERS),
//                            1, getTargetType())
                    .instant()
                    .color(16409620).radius(getDistance(level), true)
                    .particle(ParticleTypes.CRIT)
                    .spawn();

            Vec3d lookVec = entity.getLookVec();
            PacketHandler.sendToTrackingAndSelf(
                    new ParticleEffectSpawnPacket(
                            ParticleTypes.SWEEP_ATTACK,
                            ParticleEffects.SPHERE_MOTION, 16, 4,
                            entity.getPosX(), entity.getPosY() + 1.0,
                            entity.getPosZ(), 1.0, 1.0, 1.0, 1.5,
                            lookVec), (ServerPlayerEntity) entity);
        }
    }

    @Override
    public void execute(PlayerEntity entity, IMKPlayerData pData, World theWorld) {
        pData.startAbility(this);
    }
}
