package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;

import java.util.*;

public class AttributeTalentHandler extends TalentTypeHandler {

    private final Map<AttributeTalent, AttributeEntry> attributeEntryMap = new HashMap<>();

    public AttributeTalentHandler(MKPlayerData playerData) {
        super(playerData);
    }

    @Override
    public void onPersonaActivated() {
        applyAllAttributeModifiers();
    }

    @Override
    public void onPersonaDeactivated() {
        removeAllAttributeModifiers();
    }

    @Override
    public void onRecordUpdated(TalentRecord record) {
        updateTalentRecord(record, true);
    }

    @Override
    public void onRecordLoaded(TalentRecord record) {
        updateTalentRecord(record, false);
    }

    private void updateTalentRecord(TalentRecord record, boolean applyImmediately) {
//        MKCore.LOGGER.info("AttributeTalentHandler.updateTalentRecord {} {}", record, applyImmediately);

        if (record.getNode() instanceof AttributeTalentNode) {
            AttributeTalentNode node = (AttributeTalentNode) record.getNode();

            AttributeEntry entry = getAttributeEntry(node.getTalent());
            entry.updateTalent(record);
            if (applyImmediately) {
//                MKCore.LOGGER.info("AttributeTalentHandler.updateTalentRecord applying updated attribute {}", entry);
                applyAttribute(entry);
            }
        }
    }

    private void applyAttribute(AttributeEntry entry) {
//        MKCore.LOGGER.info("PlayerTalentModule.applyAttribute {}", entry);

        IAttributeInstance instance = playerData.getEntity().getAttribute(entry.getAttribute());
        //noinspection ConstantConditions
        if (instance == null) {
            MKCore.LOGGER.error("PlayerTalentModule.applyAttribute player did not have attribute {}!", entry.getAttribute());
            return;
        }

        AttributeModifier mod = entry.getModifier();
//        MKCore.LOGGER.info("PlayerTalentModule.applyAttribute mod {}", mod);
        instance.removeModifier(mod);
        instance.applyModifier(mod);

//        dumpDirtyAttributes("applyAttribute");
    }

    private void removeAttribute(AttributeTalent attributeTalent) {
        AttributeEntry entry = attributeEntryMap.get(attributeTalent);
//        MKCore.LOGGER.info("PlayerTalentModule.removeAttribute {}", entry);
        if (entry != null) {
//            MKCore.LOGGER.info("PlayerTalentModule.removeAttribute found entry {}", entry);
            IAttributeInstance instance = playerData.getEntity().getAttribute(entry.getAttribute());
            //noinspection ConstantConditions
            if (instance != null) {
                instance.removeModifier(entry.getModifier());
//                dumpDirtyAttributes("removeAttribute");
            }
        }
    }

    private void removeAllAttributeModifiers() {
//        MKCore.LOGGER.info("PlayerTalentModule.clearAttributeModifiers");
        attributeEntryMap.forEach((talent, entry) -> removeAttribute(talent));
        attributeEntryMap.clear();

//        dumpAttributes("clearAttributeModifiers");
//        dumpDirtyAttributes("clearAttributeModifiers");
    }


    private void applyAllAttributeModifiers() {
//        MKCore.LOGGER.info("PlayerTalentModule.recalculateTalentAttributes");
        attributeEntryMap.forEach((talent, entry) -> applyAttribute(entry));
    }

    private AttributeTalentHandler.AttributeEntry getAttributeEntry(AttributeTalent attribute) {
        return attributeEntryMap.computeIfAbsent(attribute, AttributeEntry::new);
    }

    private static class AttributeEntry {
        private final AttributeTalent attribute;
        private final Set<TalentRecord> records = new HashSet<>();
        private AttributeModifier modifier;
        private double value;
        private boolean dirty = true;

        public AttributeEntry(AttributeTalent attribute) {
            this.attribute = attribute;
        }

        public AttributeTalent getAttributeTalent() {
            return attribute;
        }

        public IAttribute getAttribute() {
            return attribute.getAttribute();
        }

        public Collection<TalentRecord> getRecords() {
            return records;
        }

        public AttributeModifier getModifier() {
            double rank = getTotalValue();
            if (modifier == null || modifier.getAmount() != rank) {
//                MKCore.LOGGER.info("AttributeEntry[{}] getModifier dirty {}", attribute, rank);
                modifier = attribute.createModifier(rank);
            }
            return modifier;
        }

        public double getTotalValue() {
            if (dirty) {
                value = calculateValue();
//                MKCore.LOGGER.info("AttributeEntry[{}] getTotalRank dirty {}", attribute, value);
                dirty = false;
            }
            return value;
        }

        private double calculateValue() {
            return records.stream().mapToDouble(r -> ((AttributeTalentNode) r.getNode()).getValue(r.getRank())).sum();
        }

        public void updateTalent(TalentRecord record) {
            boolean changed = records.add(record);
//            MKCore.LOGGER.info("AttributeEntry[{}] {} changed {}", attribute, record, changed);
            dirty = true;
        }

        @Override
        public String toString() {
            return "AttributeEntry{" +
                    "attribute=" + attribute +
                    ", value=" + getModifier().getAmount() +
                    ", dirty=" + dirty +
                    ", modifier=" + getModifier() +
                    '}';
        }
    }
}