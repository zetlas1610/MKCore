package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.MKCore;
import com.mojang.datafixers.Dynamic;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.regex.Pattern;

public abstract class BaseTalent extends ForgeRegistryEntry<BaseTalent> {

    public BaseTalent(ResourceLocation name) {
        setRegistryName(name);
    }

    public abstract TalentType<?> getTalentType();

    public <T> TalentNode createNode(Dynamic<T> dynamic) {
        return new TalentNode(this, dynamic);
    }

    public String getTalentName() {
        return I18n.format(String.format("%s.%s.name",
                getRegistryName().getNamespace(), getRegistryName().getPath()));
    }

    public String getTalentDescription(TalentRecord record) {
        return TextFormatting.GRAY + I18n.format(String.format("%s.%s.description",
                getRegistryName().getNamespace(), getRegistryName().getPath()));
    }

    public String getTalentTypeName() {
        return TextFormatting.GOLD + I18n.format(String.format("%s.talent_type.%s.name",
                MKCore.MOD_ID, getTalentType().toString().toLowerCase()));
    }

    public ResourceLocation getIcon() {
        return new ResourceLocation(getRegistryName().getNamespace(),
                String.format("textures/talents/%s_icon.png",
                        getRegistryName().getPath().split(Pattern.quote("."))[1]));
    }

    public ResourceLocation getFilledIcon() {
        return new ResourceLocation(getRegistryName().getNamespace(),
                String.format("textures/talents/%s_icon_filled.png",
                        getRegistryName().getPath().split(Pattern.quote("."))[1]));
    }
}
