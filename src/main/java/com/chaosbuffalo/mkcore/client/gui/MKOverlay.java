package com.chaosbuffalo.mkcore.client.gui;


import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKPlayerData;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.gui.GuiUtils;


public class MKOverlay {

    private static final ResourceLocation barTexture = MKCore.makeRL("textures/gui/abilitybar.png");
    private static final int SLOT_WIDTH = 19;
    private static final int SLOT_HEIGHT = 20;
    private static final int MANA_START_U = 21;
    private static final int MANA_START_V = 0;
    private static final int MANA_CELL_WIDTH = 3;
    private static final int MANA_CELL_HEIGHT = 8;

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
                    MANA_CELL_WIDTH, MANA_CELL_HEIGHT, 1);
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
            //        int slotCount = data.getActionBarSize();
//
//        drawCastBar(data);
//        drawBarSlots(slotCount);
//        drawAbilities(data, slotCount, event.getPartialTicks());
        });
    }
}
