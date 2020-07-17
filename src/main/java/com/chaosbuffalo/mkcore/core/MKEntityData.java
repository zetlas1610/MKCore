package com.chaosbuffalo.mkcore.core;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.nbt.CompoundNBT;

public class MKEntityData implements IMKEntityData {

    private LivingEntity entity;
    private AbilityExecutor abilityExecutor;
    private EntityStatsModule stats;
    private EntityAbilityKnowledge knowledge;

    public MKEntityData() {

    }

    public void attach(LivingEntity entity) {
        this.entity = entity;
        knowledge = new EntityAbilityKnowledge(this);
        abilityExecutor = new AbilityExecutor(this);
        stats = new EntityStatsModule(this);
        registerAttributes();
    }

    private void registerAttributes() {
        AbstractAttributeMap attributes = entity.getAttributes();
        MKAttributes.registerEntityAttributes(attributes);
    }

    public void update() {
        getAbilityExecutor().tick();
        getStats().tick();
    }

    @Override
    public LivingEntity getEntity() {
        return entity;
    }

    @Override
    public AbilityExecutor getAbilityExecutor() {
        return abilityExecutor;
    }

    @Override
    public EntityAbilityKnowledge getKnowledge() {
        return knowledge;
    }

    @Override
    public EntityStatsModule getStats() {
        return stats;
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.put("knowledge", getKnowledge().serialize());
        return tag;
    }

    @Override
    public void deserialize(CompoundNBT tag) {
        getKnowledge().deserialize(tag.getCompound("knowledge"));
    }
}
