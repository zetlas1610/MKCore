package com.chaosbuffalo.mkcore.client.gui;


import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.ClientEventHandler;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.PlayerAbilityExecutor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Arrays;

public class MKOverlay {

    private static final ResourceLocation COOLDOWN_ICON = MKCore.makeRL("textures/class/abilities/cooldown.png");

    private static final int SLOT_WIDTH = 20;
    private static final int SLOT_HEIGHT = 20;
    private static final int MIN_BAR_START_Y = 80;
    public static final int ABILITY_ICON_SIZE = 16;

    private final Minecraft mc;

    public MKOverlay() {
        mc = Minecraft.getInstance();
    }

    private void drawMana(MKPlayerData data) {
        int height = mc.getMainWindow().getScaledHeight();

        GuiTextures.CORE_TEXTURES.bind(mc);
        RenderSystem.disableLighting();

        final int maxManaPerRow = 20;
        final int manaCellWidth = 4;
        final int manaCellRowSize = 9;

        int manaStartY = height - 24 - 10;
        int manaStartX = 24;

        for (int i = 0; i < data.getStats().getMana(); i++) {
            int manaX = manaCellWidth * (i % maxManaPerRow);
            int manaY = (i / maxManaPerRow) * manaCellRowSize;
            GuiTextures.CORE_TEXTURES.drawRegionAtPos(GuiTextures.MANA_REGION, manaStartX + manaX, manaStartY + manaY);
        }
    }

    private void drawCastBar(MKPlayerData data) {
        PlayerAbilityExecutor executor = data.getAbilityExecutor();
        if (!executor.isCasting()) {
            return;
        }
        MKAbilityInfo info = data.getKnowledge().getKnownAbilityInfo(executor.getCastingAbility());
        if (info == null) {
            return;
        }
        MKAbility ability = info.getAbility();
        if (ability.getCastTime() == 0) {
            return;
        }
        int height = mc.getMainWindow().getScaledHeight();
        int castStartY = height / 2 + 8;
        int width = 50;
        int barSize = width * executor.getCastTicks() / ability.getCastTime(); // FIXME: this is wrong calc if we have spell haste
        int castStartX = mc.getMainWindow().getScaledWidth() / 2 - barSize / 2;

        GuiTextures.CORE_TEXTURES.bind(mc);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        GuiTextures.CORE_TEXTURES.drawRegionAtPosPartialWidth(GuiTextures.CAST_BAR_REGION, castStartX, castStartY, barSize);
    }

    private int getBarStartY(int slotCount) {
        int height = mc.getMainWindow().getScaledHeight();
        int barStart = height / 2 - (slotCount * SLOT_HEIGHT) / 2;
        return Math.max(barStart, MIN_BAR_START_Y);
    }

    private String getTextureForSlot(int i){
        switch (i){
            case 0:
            case 1:
            case 2:
            case 3:
                return GuiTextures.ABILITY_BAR_REG;
            default:
                return GuiTextures.ABILITY_BAR_ULT;
        }
    }

    private void drawBarSlots(int slotCount) {
        GuiTextures.CORE_TEXTURES.bind(mc);
        RenderSystem.disableLighting();
        int xOffset = 0;
        int yOffset = getBarStartY(slotCount);
        for (int i = 0; i < slotCount; i++) {
            int yPos = yOffset - i + i * SLOT_HEIGHT;
            GuiTextures.CORE_TEXTURES.drawRegionAtPos(getTextureForSlot(i), xOffset, yPos);
        }
    }

    private int drawAbilities(MKPlayerData data, MKAbility.AbilityType type, int startingSlot, int totalSlots, float partialTicks) {
        RenderSystem.disableLighting();

        final int slotAbilityOffsetX = 2;
        final int slotAbilityOffsetY = 2;

        int barStartY = getBarStartY(totalSlots);

        int slotCount = data.getKnowledge().getAbilityContainer(type).getCurrentSlotCount(type);

        float globalCooldown = ClientEventHandler.getGlobalCooldown();
        PlayerAbilityExecutor executor = data.getAbilityExecutor();

        for (int i = 0; i < slotCount; i++) {
            ResourceLocation abilityId = data.getKnowledge().getAbilityInSlot(type, i);
            if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY))
                continue;

            MKAbilityInfo info = data.getKnowledge().getKnownAbilityInfo(abilityId);
            if (info == null)
                continue;

            MKAbility ability = info.getAbility();

            float manaCost = data.getStats().getAbilityManaCost(ability);
            if (!executor.isCasting() && data.getStats().getMana() >= manaCost) {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            } else {
                RenderSystem.color4f(0.5f, 0.5f, 0.5f, 1.0F);
            }

            int slotX = slotAbilityOffsetX;
            int slotY = barStartY + slotAbilityOffsetY - (startingSlot + i) + ((startingSlot + i) * SLOT_HEIGHT);

            mc.getTextureManager().bindTexture(ability.getAbilityIcon());
            AbstractGui.blit(slotX, slotY, 0, 0, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE);

            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            float cooldownFactor = data.getAbilityExecutor().getCurrentAbilityCooldownPercent(abilityId, partialTicks);
            if (globalCooldown > 0.0f && cooldownFactor == 0) {
                cooldownFactor = globalCooldown / ClientEventHandler.getTotalGlobalCooldown();
            }

            // TODO: introduce min cooldown time so there is always a visual indicator that it's on cooldown

            if (cooldownFactor > 0) {
                int coolDownHeight = (int) (cooldownFactor * ABILITY_ICON_SIZE);
                if (coolDownHeight < 1) {
                    coolDownHeight = 1;
                }
                mc.getTextureManager().bindTexture(COOLDOWN_ICON);
                AbstractGui.blit(slotX, slotY, 0, 0, ABILITY_ICON_SIZE, coolDownHeight, ABILITY_ICON_SIZE, coolDownHeight);
            }

            ability.drawAbilityBarEffect(mc, slotX, slotY);
        }

        return startingSlot + slotCount;
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void onRenderExperienceBar(RenderGameOverlayEvent event) {
        if (event.isCancelable() || event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE) {
            return;
        }

        if (mc == null || mc.player == null)
            return;

        mc.player.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent(cap -> {

            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            drawMana(cap);
            drawCastBar(cap);

            int totalSlots = Arrays.stream(MKAbility.AbilityType.values())
                    .filter(MKAbility.AbilityType::canPlaceOnActionBar)
                    .mapToInt(type -> cap.getKnowledge().getAbilityContainer(type).getCurrentSlotCount(type))
                    .sum();

            drawBarSlots(totalSlots);
            int slot = drawAbilities(cap, MKAbility.AbilityType.Active, 0, totalSlots, event.getPartialTicks());
            slot = drawAbilities(cap, MKAbility.AbilityType.Ultimate, slot, totalSlots, event.getPartialTicks());
        });
    }
}
