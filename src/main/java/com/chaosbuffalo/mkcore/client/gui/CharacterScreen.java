package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.client.gui.widgets.*;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.LayoutRelativeWidthConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.screens.MKScreen;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.*;
import com.chaosbuffalo.mkwidgets.utils.TextureRegion;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.*;

import java.util.function.BiFunction;

public class CharacterScreen extends MKScreen {
    private final int PANEL_WIDTH = 320;
    private final int PANEL_HEIGHT = 240;
    private final int DATA_BOX_OFFSET = 78;
    private static final int NEGATIVE_COLOR = 13111115;
    private static final int POSITIVE_COLOR = 3334475;
    private static final int BASE_COLOR = 16777215;
    private boolean isDraggingAbility;
    private MKAbility dragging;
    private static final List<String> states = new ArrayList<>(Arrays.asList("stats", "damages", "abilities"));
    private static final ArrayList<IAttribute> STAT_PANEL_ATTRIBUTES = new ArrayList<>();

    static {
        STAT_PANEL_ATTRIBUTES.add(SharedMonsterAttributes.MAX_HEALTH);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.MAX_MANA);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.MANA_REGEN);
        STAT_PANEL_ATTRIBUTES.add(SharedMonsterAttributes.ARMOR);
        STAT_PANEL_ATTRIBUTES.add(SharedMonsterAttributes.ARMOR_TOUGHNESS);
        STAT_PANEL_ATTRIBUTES.add(SharedMonsterAttributes.ATTACK_DAMAGE);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.HEAL_BONUS);
        STAT_PANEL_ATTRIBUTES.add(SharedMonsterAttributes.ATTACK_SPEED);
        STAT_PANEL_ATTRIBUTES.add(SharedMonsterAttributes.MOVEMENT_SPEED);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.COOLDOWN);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.MELEE_CRIT);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.MELEE_CRIT_MULTIPLIER);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.SPELL_CRIT);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.SPELL_CRIT_MULTIPLIER);
    }

    public CharacterScreen() {
        super(new TranslationTextComponent("mk_character_screen.title"));
        isDraggingAbility = false;
        dragging = null;
    }

    public MKAbility getDragging() {
        return dragging;
    }

    public void setDragging(MKAbility dragging) {
        this.dragging = dragging;
        isDraggingAbility = true;
    }

    public void clearDragging(){
        this.dragging = null;
        isDraggingAbility = false;
    }

    public boolean isDraggingAbility() {
        return isDraggingAbility;
    }

    private MKWidget createStatList(MKPlayerData pData, int panelWidth, List<IAttribute> toDisplay) {
        if (getMinecraft().player == null){
            return null;
        }
        AbstractAttributeMap attributes = getMinecraft().player.getAttributes();
        MKStackLayoutVertical stackLayout = new MKStackLayoutVertical(0, 0, panelWidth);
        stackLayout.setMarginTop(4).setMarginBot(4).setPaddingTop(2).setMarginLeft(4)
                .setMarginRight(4).setPaddingBot(2);
        stackLayout.doSetChildWidth(true);
        for (IAttribute attr : toDisplay) {
            MKText textWidget = getTextForAttribute(attributes, attr);
            stackLayout.addWidget(textWidget);
        }
        return stackLayout;
    }


    private MKWidget createAbilitiesPage(){
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
            int slotsY = yPos + DATA_BOX_OFFSET - 28;
            int slotsX = xPos + xOffset + 4;
            MKText activesLabel = new MKText(font, "Actives:");
            activesLabel.setX(slotsX);
            activesLabel.setY(slotsY - 12);
            root.addWidget(activesLabel);
            MKLayout regularSlots = getLayoutOfAbilitySlots(slotsX, slotsY, MKAbility.AbilityType.Active, 5);
            root.addWidget(regularSlots);
            regularSlots.manualRecompute();
            int ultSlotsX = regularSlots.getX() + regularSlots.getWidth() + 30;
            MKLayout ultSlots = getLayoutOfAbilitySlots(ultSlotsX, slotsY, MKAbility.AbilityType.Ultimate, 2);
            root.addWidget(ultSlots);
            ultSlots.manualRecompute();
            MKText ultLabel = new MKText(font, "Ultimates:");
            ultLabel.setX(ultSlotsX);
            ultLabel.setY(slotsY - 12);
            root.addWidget(ultLabel);
            int passiveSlotX = ultSlots.getX() + ultSlots.getWidth() + 30;
            MKLayout passiveSlots = getLayoutOfAbilitySlots(passiveSlotX, slotsY, MKAbility.AbilityType.Passive,
                    3);
            MKText passivesLabel = new MKText(font, "Passives:");
            passivesLabel.setX(passiveSlotX);
            passivesLabel.setY(slotsY - 12);
            root.addWidget(passivesLabel);
            root.addWidget(passiveSlots);

            MKScrollView abilitiesScrollView = new MKScrollView(xPos + xOffset + 4,
                    yPos + DATA_BOX_OFFSET + 4,
                    Math.round((dataBoxRegion.width - 8) * .33f),
                    dataBoxRegion.height - 8, true);
            abilitiesScrollView.setToTop();
            abilitiesScrollView.setToRight();
            root.addWidget(abilitiesScrollView);
            MKScrollView abilityInfoScrollView = new MKScrollView(
                    abilitiesScrollView.getX() + abilitiesScrollView.getWidth() + 4,
                    yPos + DATA_BOX_OFFSET + 4,
                    Math.round((dataBoxRegion.width - 8) * .66f),
                    dataBoxRegion.height - 8, true);
            abilityInfoScrollView.setToTop();
            abilityInfoScrollView.setToRight();
            root.addWidget(abilityInfoScrollView);
            AbilityInfoWidget infoWidget = new AbilityInfoWidget(0, 0,
                    abilitiesScrollView.getWidth(), pData, font, this);
            abilityInfoScrollView.addWidget(infoWidget);
            MKRectangle rect = new MKRectangle(
                    abilitiesScrollView.getX() + abilitiesScrollView.getWidth(),
                    yPos + DATA_BOX_OFFSET + 6, 1, dataBoxRegion.height - 12, 0xffffffff);
            root.addWidget(rect);
            List<MKAbilityInfo> infos = new ArrayList<>(pData.getKnowledge().getAbilities());
            infos.sort(Comparator.comparing((info) -> info.getAbility().getAbilityName()));
            MKStackLayoutVertical stackLayout = new MKStackLayoutVertical(0, 0,
                    abilitiesScrollView.getWidth());
            stackLayout.setMarginTop(4).setMarginBot(4).setPaddingTop(2).setMarginLeft(4)
                    .setMarginRight(4).setPaddingBot(2).setPaddingRight(2);
            stackLayout.doSetChildWidth(true);
            for (MKAbilityInfo ability : infos){
                MKLayout abilityEntry = new AbilityListEntry(0, 0, 16, ability, infoWidget, font, this);
                stackLayout.addWidget(abilityEntry);
                MKRectangle div = new MKRectangle(0, 0,
                        abilitiesScrollView.getWidth() - 8, 1, 0x99ffffff);
                stackLayout.addWidget(div);
            }
            abilitiesScrollView.addWidget(stackLayout);
        });
        return root;
    }

    private MKWidget createDamageTypeList(MKPlayerData pData, int panelWidth){
        AbstractAttributeMap attributes = getMinecraft().player.getAttributes();
        MKStackLayoutVertical stackLayout = new MKStackLayoutVertical(0, 0, panelWidth);
        stackLayout.setMarginTop(4).setMarginBot(4).setPaddingTop(2).setMarginLeft(4)
                .setMarginRight(4).setPaddingBot(2);
        stackLayout.doSetChildWidth(false);
        List<MKDamageType> damageTypes = new ArrayList<>(MKCoreRegistry.DAMAGE_TYPES.getValues());
        damageTypes.sort(Comparator.comparing(MKDamageType::getDisplayName));
        for (MKDamageType damageType : damageTypes){
            if (damageType.shouldDisplay()){
                IconText iconText = new IconText(0, 0, 16,
                        damageType.getDisplayName(), damageType.getIcon(), font, 16);
                iconText.getText().setColor(0xffffffff);
                stackLayout.addConstraintToWidget(new LayoutRelativeWidthConstraint(1.0f), iconText);
                stackLayout.addWidget(iconText);
                MKRectangle rect = MKRectangle.GetHorizontalBar(1, 0xffffffff);
                stackLayout.addConstraintToWidget(new LayoutRelativeWidthConstraint(.75f), rect);
                stackLayout.addWidget(rect);
                MKText damageText = getTextForAttribute(attributes, damageType.getDamageAttribute());
                stackLayout.addConstraintToWidget(new LayoutRelativeWidthConstraint(1.0f), damageText);
                stackLayout.addWidget(damageText);
                MKText resistanceText = getTextForAttribute(attributes, damageType.getResistanceAttribute());
                stackLayout.addConstraintToWidget(new LayoutRelativeWidthConstraint(1.0f), resistanceText);
                stackLayout.addWidget(resistanceText);
                MKRectangle rect2 = MKRectangle.GetHorizontalBar(1, 0xffffffff);
                stackLayout.addConstraintToWidget(new LayoutRelativeWidthConstraint(.75f), rect2);
                stackLayout.addWidget(rect2);
            }
        }
        return stackLayout;
    }

    private MKText getTextForAttribute(AbstractAttributeMap attributes, IAttribute attr) {
        IAttributeInstance attribute = attributes.getAttributeInstance(attr);
        String text = String.format("%s: %.2f", I18n.format(String.format("attribute.name.%s",
                attribute.getAttribute().getName())), attribute.getValue());
        MKText textWidget = new MKText(minecraft.fontRenderer, text).setMultiline(true);
        addPreDrawRunnable(() -> {
            String newText = String.format("%s: %.2f", I18n.format(String.format("attribute.name.%s",
                    attribute.getAttribute().getName())), attribute.getValue());
            textWidget.setText(newText);
            if (attribute.getValue() < attribute.getBaseValue()) {
                textWidget.setColor(NEGATIVE_COLOR);
            } else if (attribute.getValue() > attribute.getBaseValue()) {
                textWidget.setColor(POSITIVE_COLOR);
            } else {
                textWidget.setColor(BASE_COLOR);
            }
        });
       return textWidget;
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

    private MKLayout getLayoutOfAbilitySlots(int x, int y, MKAbility.AbilityType slotType, int count){
        MKStackLayoutHorizontal layout = new MKStackLayoutHorizontal(x, y, 24);
        layout.setPaddings(2, 2, 0, 0);
        layout.setMargins(2, 2, 2, 2);
        for (int i = 0; i < count; i++){
            AbilitySlotWidget slot = new AbilitySlotWidget(0, 0, slotType, i,  this);
            layout.addWidget(slot);
        }
        return layout;
    }


    private MKLayout createScrollingPanelWithContent(BiFunction<MKPlayerData, Integer, MKWidget> contentCreator){
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

    @Override
    public void setupScreen() {
        super.setupScreen();
        addState("stats", () -> createScrollingPanelWithContent((pData, width) ->
                createStatList(pData, width, STAT_PANEL_ATTRIBUTES)));
        addState("damages", () -> createScrollingPanelWithContent(this::createDamageTypeList));
        addState("abilities", this::createAbilitiesPage);
        pushState("stats");

    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        boolean handled = super.mouseReleased(mouseX, mouseY, mouseButton);
        if (isDraggingAbility){
            clearDragging();
            clearDragState();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, mouseButton);
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
}
