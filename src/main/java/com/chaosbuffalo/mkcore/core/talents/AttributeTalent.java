package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.MKCore;
import com.mojang.datafixers.Dynamic;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.util.UUID;

public class AttributeTalent extends BaseTalent {
    private final UUID id;
    private final RangedAttribute attribute;
    private AttributeModifier.Operation operation;
    private boolean renderAsPercentage;
    private double defaultPerRank;

    public AttributeTalent(ResourceLocation name, RangedAttribute attr, UUID id) {
        super(name);
        this.id = id;
        this.attribute = attr;
        this.operation = AttributeModifier.Operation.ADDITION;
        this.renderAsPercentage = false;
        defaultPerRank = 1;
    }

    public RangedAttribute getAttribute() {
        return attribute;
    }

    public UUID getUUID() {
        return id;
    }

    public AttributeModifier.Operation getOp() {
        return operation;
    }

    public AttributeTalent setOp(AttributeModifier.Operation value) {
        operation = value;
        return this;
    }

    public AttributeTalent setDisplayAsPercentage(boolean usePercentage) {
        renderAsPercentage = usePercentage;
        return this;
    }

    public AttributeTalent setDefaultPerRank(double valuePerRank) {
        defaultPerRank = valuePerRank;
        return this;
    }

    @Override
    public String toString() {
        return String.format("AttributeTalent[%s, %s, %s]", attribute.getName(), id, operation);
    }

    private String getDescriptionTranslationKey() {
        return String.format("%s.%s.description", getRegistryName().getNamespace(), getRegistryName().getPath());
    }

    @Override
    public String getTalentDescription(TalentRecord record) {
        double perRank = 0;
        double currentValue = 0;
        if (record.getNode() instanceof AttributeTalentNode) {
            AttributeTalentNode attrNode = (AttributeTalentNode) record.getNode();
            perRank = attrNode.getPerRank();
            currentValue = record.getRank() * perRank;
        } else {
            MKCore.LOGGER.error("Trying to create a tooltip for {} but the node was not an AttributeTalentNode!", this);
        }
        String amount;
        String totalAmount;
        if (renderAsPercentage) {
            amount = String.format("%.2f%%", perRank * 100);
            totalAmount = String.format("%.2f%%", currentValue * 100);
        } else {
            amount = String.format("%.2f", perRank);
            totalAmount = String.format("%.2f", currentValue);
        }
        String finalAmount = String.format("%s (%s)", amount, totalAmount);
        return TextFormatting.GRAY + I18n.format(getDescriptionTranslationKey(), finalAmount);
    }

    public AttributeModifier createModifier(double value) {
        return new AttributeModifier(getUUID(), getRegistryName().toString(), value, getOp()).setSaved(false);
    }

    public double getDefaultPerRank() {
        return defaultPerRank;
    }

    @Override
    public TalentType<?> getTalentType() {
        return TalentType.ATTRIBUTE;
    }

    @Override
    public <T> TalentNode createNode(Dynamic<T> dynamic) {
        return new AttributeTalentNode(this, dynamic);
    }
}
