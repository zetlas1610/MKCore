package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.client.gui.widgets.AbilityInfoWidget;
import com.chaosbuffalo.mkcore.client.gui.widgets.AbilityListEntry;
import com.chaosbuffalo.mkcore.client.gui.widgets.ScrollingListPanelLayout;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.screens.MKScreen;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKRectangle;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKWidget;
import com.chaosbuffalo.mkwidgets.utils.TextureRegion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;

import java.util.*;
import java.util.function.BiFunction;

public abstract class AbilityPanelScreen extends MKScreen implements IPlayerDataAwareScreen {

    protected static final int NEGATIVE_COLOR = 13111115;
    protected static final int POSITIVE_COLOR = 3334475;
    protected static final int BASE_COLOR = 16777215;
    protected final List<String> states = new ArrayList<>();
    protected final int PANEL_WIDTH = 320;
    protected final int PANEL_HEIGHT = 240;
    protected final int DATA_BOX_OFFSET = 78;
    protected ScrollingListPanelLayout currentScrollingPanel;
    protected boolean wasResized;
    protected boolean isDraggingAbility;
    protected MKAbility dragging;
    protected AbilityInfoWidget infoWidget;
    protected ScrollingListPanelLayout abilitiesScrollPanel;
    private MKAbilityInfo abilityInfo;
    private boolean doAbilityDrag;

    public AbilityPanelScreen(ITextComponent title) {
        super(title);
        wasResized = false;
        isDraggingAbility = false;
        dragging = null;
        setDoAbilityDrag(false);
    }

    public boolean shouldAbilityDrag(){
        return doAbilityDrag;
    }

    public void setDoAbilityDrag(boolean doAbilityDrag) {
        this.doAbilityDrag = doAbilityDrag;
    }

    protected MKLayout getRootLayout(int xPos, int yPos, int xOffset, int width){
        MKLayout root = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        root.setMargins(5, 5, 5, 5);
        root.setPaddingTop(5).setPaddingBot(5);
        MKLayout statebuttons = getStateButtons(xPos + xOffset, yPos + 8, width);
        root.addWidget(statebuttons);
        return root;
    }

    private MKLayout getStateButtons(int xPos, int yPos, int width){
        MKLayout layout = new MKStackLayoutHorizontal(xPos, yPos, 24);
        layout.setMarginLeft(4).setMarginRight(4).setMarginTop(2).setMarginBot(2)
                .setPaddingLeft(2).setPaddingRight(2);
        for (String state : states){
            MKButton button = new MKButton(I18n.format(String.format("mkcore.gui.character.%s", state)));
            button.setWidth(60);
            if (getState().equals(state)){
                button.setEnabled(false);
            }
            addPreDrawRunnable(() -> {
                if (state.equals(getState())){
                    button.setEnabled(false);
                } else {
                    button.setEnabled(true);
                }
            });
            button.setPressedCallback((btn, mouseButton)-> {
                pushState(state);
                return true;
            });
            layout.addWidget(button);
        }
        return layout;
    }

    protected MKLayout createScrollingPanelWithContent(BiFunction<MKPlayerData, Integer, MKWidget> contentCreator){
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        MKLayout root = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        root.setMargins(5, 5, 5, 5);
        root.setPaddingTop(5).setPaddingBot(5);
        TextureRegion dataBoxRegion = GuiTextures.CORE_TEXTURES.getRegion(GuiTextures.DATA_BOX);
        int xOffset = GuiTextures.CORE_TEXTURES.getCenterXOffset(GuiTextures.DATA_BOX, GuiTextures.BACKGROUND_320_240);
        if (minecraft == null || minecraft.player == null || dataBoxRegion == null){
            return root;
        }
        MKLayout statebuttons = getStateButtons(xPos + xOffset, yPos + 8,
                dataBoxRegion.width);
        root.addWidget(statebuttons);
        minecraft.player.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent((pData) -> {
            // Stat Panel
            MKScrollView statScrollView = new MKScrollView(xPos + xOffset + 4,
                    yPos + DATA_BOX_OFFSET + 4,
                    dataBoxRegion.width - 8, dataBoxRegion.height - 8, true);
            statScrollView.addWidget(contentCreator.apply(pData, dataBoxRegion.width - 8));
            statScrollView.setToTop();
            statScrollView.setToRight();
            root.addWidget(statScrollView);
        });
        return root;
    }

    protected void restoreScrollingPanelState(){
        if (currentScrollingPanel != null){
            double offsetX = currentScrollingPanel.getContentScrollView().getOffsetX();
            double offsetY = currentScrollingPanel.getContentScrollView().getOffsetY();
            double listOffsetX = currentScrollingPanel.getListScrollView().getOffsetX();
            double listOffsetY = currentScrollingPanel.getListScrollView().getOffsetY();
            addPostSetupCallback(() -> {
                if (currentScrollingPanel != null){
                    currentScrollingPanel.getContentScrollView().setOffsetX(offsetX);
                    currentScrollingPanel.getContentScrollView().setOffsetY(offsetY);
                    currentScrollingPanel.getListScrollView().setOffsetX(listOffsetX);
                    currentScrollingPanel.getListScrollView().setOffsetY(listOffsetY);
                    if (wasResized){
                        MKCore.LOGGER.info("Setting scrollviews back to top because resize");
                        currentScrollingPanel.getListScrollView().setToRight();
                        currentScrollingPanel.getListScrollView().setToTop();
                        currentScrollingPanel.getContentScrollView().setToTop();
                        currentScrollingPanel.getContentScrollView().setToRight();
                        wasResized = false;
                    }
                }
            });
        } else {
            addPostSetupCallback(() -> {
                wasResized = false;
            });
        }
    }

    public ScrollingListPanelLayout getAbilityScrollPanel(int xPos, int yPos, int width, int height, MKPlayerData pData){
        ScrollingListPanelLayout panel = new ScrollingListPanelLayout(
                xPos, yPos, width, height);
        AbilityInfoWidget infoWidget = new AbilityInfoWidget(0, 0,
                panel.getContentScrollView().getWidth(), pData, font, this);
        this.infoWidget = infoWidget;
        panel.setContent(infoWidget);
        MKStackLayoutVertical stackLayout = new MKStackLayoutVertical(0, 0,
                panel.getListScrollView().getWidth());
        stackLayout.setMarginTop(4).setMarginBot(4).setPaddingTop(2).setMarginLeft(4)
                .setMarginRight(4).setPaddingBot(2).setPaddingRight(2);
        stackLayout.doSetChildWidth(true);
        pData.getKnowledge().getKnownAbilities().getKnownStream()
                .sorted(Comparator.comparing((info) -> info.getAbility().getAbilityName()))
                .forEach(ability -> {
                    MKLayout abilityEntry = new AbilityListEntry(0, 0, 16, ability,
                            infoWidget, font, this);
                    stackLayout.addWidget(abilityEntry);
                    MKRectangle div = new MKRectangle(0, 0,
                            panel.getListScrollView().getWidth() - 8, 1, 0x99ffffff);
                    stackLayout.addWidget(div);
                });
        panel.setList(stackLayout);
        return panel;
    }

    @Override
    public void onPlayerDataUpdate() {
        MKCore.LOGGER.info("CharacterScreen.onPlayerDataUpdate");
        flagNeedSetup();
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        wasResized = true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public MKAbility getDragging() {
        return dragging;
    }

    public void setDragging(MKAbility dragging) {
        this.dragging = dragging;
        isDraggingAbility = true;
    }

    public void setAbilityInfo(MKAbilityInfo abilityInfo) {
        this.abilityInfo = abilityInfo;
    }

    public MKAbilityInfo getAbilityInfo() {
        return abilityInfo;
    }

    public void clearDragging(){
        this.dragging = null;
        isDraggingAbility = false;
    }

    public boolean isDraggingAbility() {
        return isDraggingAbility;
    }
}
