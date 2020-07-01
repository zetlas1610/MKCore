package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.client.gui.widgets.ScrollingListPanelLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKWidget;
import com.chaosbuffalo.mkwidgets.utils.TextureRegion;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class LearnAbilitiesScreen extends AbilityPanelScreen {
    private List<MKAbility> abilities;

    public LearnAbilitiesScreen(ITextComponent title, List<MKAbility> abilities) {
        super(title);
        this.abilities = abilities;
        states.add("choose_ability");
    }

    private MKWidget createAbilitiesPage(){
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        TextureRegion dataBoxRegion = GuiTextures.CORE_TEXTURES.getRegion(GuiTextures.DATA_BOX);
        if (minecraft == null || minecraft.player == null || dataBoxRegion == null){
            return new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        }
        int xOffset = GuiTextures.CORE_TEXTURES.getCenterXOffset(
                GuiTextures.DATA_BOX, GuiTextures.BACKGROUND_320_240);
        MKLayout root = getRootLayout(xPos, yPos, xOffset, dataBoxRegion.width, true);
        minecraft.player.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent((pData) -> {
            int contentX = xPos + xOffset;
            int contentY = yPos + DATA_BOX_OFFSET;
            int contentWidth = dataBoxRegion.width;
            int contentHeight = dataBoxRegion.height;
            ScrollingListPanelLayout panel = getAbilityScrollPanel(contentX, contentY,
                    contentWidth, contentHeight, pData, abilities);
            currentScrollingPanel = panel;
            abilitiesScrollPanel = panel;
            root.addWidget(panel);
        });
        return root;
    }

    @Override
    public void setupScreen() {
        super.setupScreen();
        infoWidget = null;
        currentScrollingPanel = null;
        addState("choose_ability", this::createAbilitiesPage);
        pushState("choose_ability");
    }
}
