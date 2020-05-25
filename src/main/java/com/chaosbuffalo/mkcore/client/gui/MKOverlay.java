package com.chaosbuffalo.mkcore.client.gui;


import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.ClientEventHandler;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.PlayerAbility;
import com.chaosbuffalo.mkcore.abilities.PlayerAbilityInfo;
import com.chaosbuffalo.mkcore.core.IMKPlayerData;
import com.chaosbuffalo.mkcore.core.PlayerAbilityExecutor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.gui.GuiUtils;


public class MKOverlay {

    private static final ResourceLocation barTexture = MKCore.makeRL("textures/gui/abilitybar.png");
    private static final ResourceLocation COOLDOWN_ICON = MKCore.makeRL("textures/class/abilities/cooldown.png");

    private static final int SLOT_WIDTH = 19;
    private static final int SLOT_HEIGHT = 20;
    private static final int MANA_START_U = 21;
    private static final int MANA_START_V = 0;
    private static final int MANA_CELL_WIDTH = 3;
    private static final int MANA_CELL_HEIGHT = 8;
    private static final int MIN_BAR_START_Y = 80;
    public static final int ABILITY_ICON_SIZE = 16;

    private final Minecraft mc;

    public MKOverlay() {
        mc = Minecraft.getInstance();
    }

    private void drawMana(IMKPlayerData data) {
        int height = mc.getMainWindow().getScaledHeight();

        mc.getTextureManager().bindTexture(barTexture);
        RenderSystem.disableLighting();

        final int maxManaPerRow = 20;
        final int manaCellWidth = 4;
        final int manaCellRowSize = 9;

        int manaStartY = height - 24 - 10;
        int manaStartX = 24;

        for (int i = 0; i < data.getMana(); i++) {
            int manaX = manaCellWidth * (i % maxManaPerRow);
            int manaY = (i / maxManaPerRow) * manaCellRowSize;
            GuiUtils.drawTexturedModalRect(manaStartX + manaX, manaStartY + manaY, MANA_START_U, MANA_START_V,
                    MANA_CELL_WIDTH, MANA_CELL_HEIGHT, 0f);
        }
    }

    private void drawCastBar(IMKPlayerData data) {
        PlayerAbilityExecutor executor = data.getAbilityExecutor();
        if (!executor.isCasting()) {
            return;
        }
        PlayerAbilityInfo info = data.getAbilityInfo(executor.getCastingAbility());
        if (info == null || !info.isCurrentlyKnown()) {
            return;
        }
        PlayerAbility ability = info.getAbility();
        int height = mc.getMainWindow().getScaledHeight();
        int castStartY = height / 2 + 8;
        int width = 50;
        int barSize = width * executor.getCastTicks() / ability.getCastTime(info.getRank()); // FIXME: this is wrong calc if we have spell haste
        int castStartX = mc.getMainWindow().getScaledWidth() / 2 - barSize / 2;

        mc.getTextureManager().bindTexture(barTexture);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        GuiUtils.drawTexturedModalRect(castStartX, castStartY, 26, 21, barSize, 3, 0f);
    }

    private int getBarStartY(int slotCount) {
        int height = mc.getMainWindow().getScaledHeight();
        int barStart = height / 2 - (slotCount * SLOT_HEIGHT) / 2;
        return Math.max(barStart, MIN_BAR_START_Y);
    }

    private void drawBarSlots(int slotCount) {
        this.mc.getTextureManager().bindTexture(barTexture);
        RenderSystem.disableLighting();

        int xOffset = 0;
        int yOffset = getBarStartY(slotCount);
        for (int i = 0; i < slotCount; i++) {
            GuiUtils.drawTexturedModalRect(xOffset, yOffset + i * SLOT_HEIGHT, 0, 0, SLOT_WIDTH, SLOT_HEIGHT, 0f);
        }
    }

    private void drawAbilities(IMKPlayerData data, int slotCount, float partialTicks) {
        RenderSystem.disableLighting();

        final int slotAbilityOffsetX = 1;
        final int slotAbilityOffsetY = 2;

        int barStartY = getBarStartY(slotCount);

        float globalCooldown = ClientEventHandler.getGlobalCooldown();

        for (int i = 0; i < slotCount; i++) {
            ResourceLocation abilityId = data.getAbilityInSlot(i);
            if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY))
                continue;

            PlayerAbilityInfo info = data.getAbilityInfo(abilityId);
            if (info == null || !info.isCurrentlyKnown())
                continue;

            PlayerAbility ability = info.getAbility();
            if (ability == null)
                continue;

            float manaCost = data.getAbilityManaCost(abilityId);
            if (!data.isCasting() && data.getMana() >= manaCost) {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            } else {
                RenderSystem.color4f(0.5f, 0.5f, 0.5f, 1.0F);
            }

            int slotX = slotAbilityOffsetX;
            int slotY = barStartY + slotAbilityOffsetY + (i * SLOT_HEIGHT);

            mc.getTextureManager().bindTexture(ability.getAbilityIcon());
            AbstractGui.blit(slotX, slotY, 0, 0, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE);

            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            float cooldownFactor = data.getCooldownPercent(info, partialTicks);
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
            int slotCount = cap.getActionBarSize();
            drawBarSlots(slotCount);
            drawAbilities(cap, slotCount, event.getPartialTicks());
        });
    }
}
