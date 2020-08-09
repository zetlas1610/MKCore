package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.CoreCapabilities;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
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
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;

import java.util.function.BiConsumer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
    private MKAbility ability;
    private boolean doAbilityDrag;

    public AbilityPanelScreen(ITextComponent title) {
        super(title);
        wasResized = false;
        isDraggingAbility = false;
        dragging = null;
        setDoAbilityDrag(false);
    }

    public boolean shouldAbilityDrag() {
        return doAbilityDrag;
    }

    public void setDoAbilityDrag(boolean doAbilityDrag) {
        this.doAbilityDrag = doAbilityDrag;
    }

    protected MKLayout getRootLayout(int xPos, int yPos, int xOffset, int width, boolean addStateButtons) {
        MKLayout root = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        root.setMargins(5, 5, 5, 5);
        root.setPaddingTop(5).setPaddingBot(5);
        if (addStateButtons) {
            MKLayout statebuttons = getStateButtons(xPos + xOffset, yPos + 8, width);
            root.addWidget(statebuttons);
        }
        return root;
    }

    @Override
    public void setupScreen() {
        super.setupScreen();
        this.ability = null;
    }

    @Override
    public void addRestoreStateCallbacks() {
        super.addRestoreStateCallbacks();
        if (ability != null) {
            final MKAbility abilityInf = getAbility();
            addPostSetupCallback(() -> {
                setAbility(abilityInf);
            });
        }
        restoreScrollingPanelState();
    }

    private MKLayout getStateButtons(int xPos, int yPos, int width) {
        MKLayout layout = new MKStackLayoutHorizontal(xPos, yPos, 24);
        layout.setMarginLeft(4).setMarginRight(4).setMarginTop(2).setMarginBot(2)
                .setPaddingLeft(2).setPaddingRight(2);
        for (String state : states) {
            MKButton button = new MKButton(I18n.format(String.format("mkcore.gui.character.%s", state)));
            button.setWidth(60);
            if (getState().equals(state)) {
                button.setEnabled(false);
            }
            addPreDrawRunnable(() -> {
                if (state.equals(getState())) {
                    button.setEnabled(false);
                } else {
                    button.setEnabled(true);
                }
            });
            button.setPressedCallback((btn, mouseButton) -> {
                pushState(state);
                return true;
            });
            layout.addWidget(button);
        }
        return layout;
    }

    protected MKLayout createScrollingPanelWithContent(BiFunction<MKPlayerData, Integer, MKWidget> contentCreator,
                                                       BiConsumer<MKPlayerData, MKLayout> headerCreator){
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        MKLayout root = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        root.setMargins(5, 5, 5, 5);
        root.setPaddingTop(5).setPaddingBot(5);
        TextureRegion dataBoxRegion = GuiTextures.CORE_TEXTURES.getRegion(GuiTextures.DATA_BOX);
        int xOffset = GuiTextures.CORE_TEXTURES.getCenterXOffset(GuiTextures.DATA_BOX, GuiTextures.BACKGROUND_320_240);
        if (minecraft == null || minecraft.player == null || dataBoxRegion == null) {
            return root;
        }
        MKLayout statebuttons = getStateButtons(xPos + xOffset, yPos + 8,
                dataBoxRegion.width);
        root.addWidget(statebuttons);
        minecraft.player.getCapability(CoreCapabilities.PLAYER_CAPABILITY).ifPresent((pData) -> {
            // Stat Panel
            MKLayout headerLayout = new MKLayout(xPos, statebuttons.getY() + statebuttons.getHeight(), PANEL_WIDTH,
                    DATA_BOX_OFFSET - statebuttons.getHeight() - 8);
            headerLayout.setMargins(4, 4, 0, 0);
            headerCreator.accept(pData, headerLayout);
            root.addWidget(headerLayout);
            MKScrollView scrollView = new MKScrollView(xPos + xOffset + 4,
                    yPos + DATA_BOX_OFFSET + 4,
                    dataBoxRegion.width - 8, dataBoxRegion.height - 8, true);
            scrollView.addWidget(contentCreator.apply(pData, dataBoxRegion.width - 8));
            scrollView.setToTop();
            scrollView.setToRight();
            root.addWidget(scrollView);
        });
        return root;
    }

    protected void restoreScrollingPanelState() {
        if (currentScrollingPanel != null) {
            double offsetX = currentScrollingPanel.getContentScrollView().getOffsetX();
            double offsetY = currentScrollingPanel.getContentScrollView().getOffsetY();
            double listOffsetX = currentScrollingPanel.getListScrollView().getOffsetX();
            double listOffsetY = currentScrollingPanel.getListScrollView().getOffsetY();
            addPostSetupCallback(() -> {
                if (currentScrollingPanel != null) {
                    currentScrollingPanel.getContentScrollView().setOffsetX(offsetX);
                    currentScrollingPanel.getContentScrollView().setOffsetY(offsetY);
                    currentScrollingPanel.getListScrollView().setOffsetX(listOffsetX);
                    currentScrollingPanel.getListScrollView().setOffsetY(listOffsetY);
                    if (wasResized) {
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

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        GuiTextures.CORE_TEXTURES.bind(getMinecraft());
        RenderSystem.disableLighting();
        GuiTextures.CORE_TEXTURES.drawRegionAtPos(GuiTextures.BACKGROUND_320_240, xPos, yPos);
        int xOffset = GuiTextures.CORE_TEXTURES.getCenterXOffset(GuiTextures.DATA_BOX, GuiTextures.BACKGROUND_320_240);
        GuiTextures.CORE_TEXTURES.drawRegionAtPos(GuiTextures.DATA_BOX, xPos + xOffset, yPos + DATA_BOX_OFFSET);
        super.render(mouseX, mouseY, partialTicks);
        RenderSystem.enableLighting();
    }

    public ScrollingListPanelLayout getAbilityScrollPanel(int xPos, int yPos, int width, int height,
                                                          MKPlayerData pData, List<MKAbility> abilities) {
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
        abilities.stream()
                .sorted(Comparator.comparing(MKAbility::getAbilityName))
                .forEach(ability -> {
                    MKLayout abilityEntry = new AbilityListEntry(0, 0, 16,
                            ability, infoWidget, font, this);
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

    public void setAbility(MKAbility ability) {
        this.ability = ability;
    }

    public MKAbility getAbility() {
        return ability;
    }

    public void clearDragging() {
        this.dragging = null;
        isDraggingAbility = false;
    }

    public boolean isDraggingAbility() {
        return isDraggingAbility;
    }
}
