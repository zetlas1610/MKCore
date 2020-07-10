package com.chaosbuffalo.mkcore.mku.entity;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.training.*;
import com.chaosbuffalo.mkcore.mku.abilities.*;
import com.chaosbuffalo.mkcore.mku.entity.ai.*;
import com.chaosbuffalo.mkcore.mku.entity.ai.controller.MovementStrategyController;
import com.chaosbuffalo.mkcore.mku.entity.ai.memory.MKUMemoryModuleTypes;
import com.chaosbuffalo.mkcore.network.OpenLearnAbilitiesGuiPacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;


public class GreenLadyEntity extends MKEntity implements IAbilityTrainingEntity {
    private int timesDone;
    private final EntityAbilityTrainer abilityTrainer;

    public GreenLadyEntity(EntityType<? extends GreenLadyEntity> type, World worldIn) {
        super(type, worldIn);
        timesDone = 0;
        abilityTrainer = new EntityAbilityTrainer(this);
        abilityTrainer.addTrainedAbility(EmberAbility.INSTANCE);
        abilityTrainer.addTrainedAbility(ClericHeal.INSTANCE)
                .addRequirement(new HeldItemRequirement(Items.ROTTEN_FLESH, Hand.MAIN_HAND))
                .addRequirement(new HeldItemRequirement(Items.ROTTEN_FLESH, Hand.OFF_HAND));
        abilityTrainer.addTrainedAbility(WhirlwindBlades.INSTANCE)
                .addRequirement(new HeldItemRequirement(Items.DIAMOND_SWORD, Hand.MAIN_HAND))
                .addRequirement(new ExperienceLevelRequirement(30));
    }

    @Nullable
    @Override
    public ILivingEntityData onInitialSpawn(IWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason,
                                            @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
        this.getCapability(Capabilities.ENTITY_CAPABILITY).ifPresent(
                mkEntityData -> {
                    mkEntityData.getKnowledge().learnAbility(EmberAbility.INSTANCE, 2);
                    mkEntityData.getKnowledge().learnAbility(FireArmor.INSTANCE);
                    mkEntityData.getKnowledge().learnAbility(ClericHeal.INSTANCE);
                    mkEntityData.getKnowledge().learnAbility(SkinLikeWoodAbility.INSTANCE);
                });
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.3);
        MovementStrategyController.enterCastingMode(this, 5.0);
        return super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);

    }

    @Override
    public ActionResultType applyPlayerInteraction(PlayerEntity player, Vec3d vec, Hand hand) {
        if (!player.getEntityWorld().isRemote()) {
            MKCore.getPlayer(player).ifPresent(playerData -> {
                PacketHandler.sendMessage(new OpenLearnAbilitiesGuiPacket(playerData, abilityTrainer), (ServerPlayerEntity) player);
            });
        }
        return ActionResultType.SUCCESS;
    }


    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(7, new LookAtThreatTargetGoal(this));
        this.targetSelector.addGoal(2, new TargetEnemyGoal(this, true,
                true));
        this.goalSelector.addGoal(1, new MovementGoal(this));
        this.goalSelector.addGoal(3, new MKMeleeAttackGoal(this, .25));
        this.goalSelector.addGoal(2, new UseAbilityGoal(this));
    }


    @Override
    public void enterDefaultMovementState(LivingEntity target) {
        this.brain.setMemory(MKUMemoryModuleTypes.MOVEMENT_TARGET, target);
        MovementStrategyController.enterCastingMode(this, 5.0);
    }

    @Override
    public IAbilityTrainer getAbilityTrainer() {
        return abilityTrainer;
    }
}
