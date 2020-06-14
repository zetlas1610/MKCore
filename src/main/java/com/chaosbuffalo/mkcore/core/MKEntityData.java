package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.mku.entity.MKEntity;
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
        if (entity instanceof MKEntity){
            MKEntity mkEntity = (MKEntity) entity;
            abilityExecutor.setStartCastCallback(mkEntity::startCast);
            abilityExecutor.setCompleteAbilityCallback(mkEntity::endCast);
        }
        registerAttributes();
    }

    private void registerAttributes() {
        AbstractAttributeMap attributes = entity.getAttributes();
        attributes.registerAttribute(MKAttributes.COOLDOWN);
        attributes.registerAttribute(MKAttributes.HEAL_BONUS);
        for (MKDamageType damageType : MKCoreRegistry.DAMAGE_TYPES.getValues()) {
            damageType.addAttributes(attributes);
        }
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
    public void serialize(CompoundNBT nbt) {
        CompoundNBT knowledgeTag = new CompoundNBT();
        getKnowledge().serialize(knowledgeTag);
        nbt.put("knowledge", knowledgeTag);
    }

    @Override
    public void deserialize(CompoundNBT nbt) {
        if (nbt.contains("knowledge")){
            getKnowledge().deserialize(nbt.getCompound("knowledge"));
        }
    }
}
