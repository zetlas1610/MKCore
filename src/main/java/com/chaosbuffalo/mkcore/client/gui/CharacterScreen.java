package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.client.gui.widgets.*;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.core.talents.TalentTreeRecord;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.LayoutRelativeWidthConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.screens.MKScreen;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.*;
import com.chaosbuffalo.mkwidgets.utils.TextureRegion;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.*;

import java.util.function.BiFunction;

public class CharacterScreen extends MKScreen implements IPlayerDataAwareScreen {
    private final int PANEL_WIDTH = 320;
    private final int PANEL_HEIGHT = 240;
    private final int DATA_BOX_OFFSET = 78;
    private static final int NEGATIVE_COLOR = 13111115;
    private static final int POSITIVE_COLOR = 3334475;
    private static final int BASE_COLOR = 16777215;
    private boolean isDraggingAbility;
    private MKAbility dragging;
    private AbilityInfoWidget infoWidget;
    private MKAbilityInfo abilityInfo;
    private TalentTreeWidget talentTreeWidget;
    private TalentTreeRecord currentTree;
    private ScrollingListPanelLayout currentScrollingPanel;
    private ScrollingListPanelLayout talentScrollPanel;
    private ScrollingListPanelLayout abilitiesScrollPanel;
    boolean wasResized;
    private int scrollOffsetX;
    private int scrollOffsetY;
    private static final List<String> states = new ArrayList<>(Arrays.asList(
            "stats", "damages", "abilities", "talents"));
    private static final ArrayList<IAttribute> STAT_PANEL_ATTRIBUTES = new ArrayList<>();
    public static class AbilitySlotKey {
        public MKAbility.AbilityType type;
        public int slot;

        public AbilitySlotKey(MKAbility.AbilityType type, int index){
            this.type = type;
            this.slot = index;
        }

        @Override
        public int hashCode() {
            return slot + type.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof AbilitySlotKey){
                AbilitySlotKey otherKey = (AbilitySlotKey)other;
                return slot == otherKey.slot && type.equals(otherKey.type);
            }
            return false;
        }
    }
    private final Map<AbilitySlotKey, AbilitySlotWidget> abilitySlots;

    public List<AbilitySlotWidget> getSlotsForType(MKAbility.AbilityType slotType){
        List<AbilitySlotWidget> widgets = new ArrayList<>();
        for (AbilitySlotWidget slot : abilitySlots.values()){
            if (slot.getSlotType().equals(slotType)){
                widgets.add(slot);
            }
        }
        return widgets;
    }

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
        abilitySlots = new HashMap<>();
        isDraggingAbility = false;
        dragging = null;
        scrollOffsetX = 0;
        scrollOffsetY = 0;
        wasResized = false;
    }

    public MKAbility getDragging() {
        return dragging;
    }

    public void setDragging(MKAbility dragging) {
        this.dragging = dragging;
        isDraggingAbility = true;
        Set<MKAbility.AbilityType> types = Sets.newHashSet(MKAbility.AbilityType.Active, MKAbility.AbilityType.Passive,
                MKAbility.AbilityType.Ultimate);
        types.remove(dragging.getType());
        for (MKAbility.AbilityType type : types){
            for (AbilitySlotWidget widget : getSlotsForType(type)){
                widget.setBackgroundColor(0xff555555);
            }
        }
    }

    public void setAbilityInfo(MKAbilityInfo abilityInfo) {
        this.abilityInfo = abilityInfo;
    }

    public MKAbilityInfo getAbilityInfo() {
        return abilityInfo;
    }

    public void clearDragging(){
        for (AbilitySlotWidget widget : abilitySlots.values()){
            widget.setBackgroundColor(0xffffffff);
        }
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

    private MKLayout getRootLayout(int xPos, int yPos, int xOffset, int width){
        MKLayout root = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        root.setMargins(5, 5, 5, 5);
        root.setPaddingTop(5).setPaddingBot(5);
        MKLayout statebuttons = getStateButtons(xPos + xOffset, yPos + 8, width);
        root.addWidget(statebuttons);
        return root;
    }

    private MKWidget createTalentsPage(){
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        TextureRegion dataBoxRegion = GuiTextures.CORE_TEXTURES.getRegion(GuiTextures.DATA_BOX);
        if (minecraft == null || minecraft.player == null || dataBoxRegion == null){
            return new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        }
        int xOffset = GuiTextures.CORE_TEXTURES.getCenterXOffset(
                GuiTextures.DATA_BOX, GuiTextures.BACKGROUND_320_240);
        MKLayout root = getRootLayout(xPos, yPos, xOffset, dataBoxRegion.width);
        minecraft.player.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent((pData) -> {
            int contentX = xPos + xOffset;
            int contentY = yPos + DATA_BOX_OFFSET;
            int contentWidth = dataBoxRegion.width;
            int contentHeight = dataBoxRegion.height;
            ScrollingListPanelLayout panel = new ScrollingListPanelLayout(
                    contentX, contentY, contentWidth, contentHeight);
            currentScrollingPanel = panel;
            talentScrollPanel = panel;
            TalentTreeWidget treeWidget = new TalentTreeWidget(0, 0,
                    panel.getContentScrollView().getWidth(),
                    panel.getContentScrollView().getHeight(), pData, font, this);
            talentTreeWidget = treeWidget;
            panel.setContent(treeWidget);
            MKStackLayoutVertical stackLayout = new MKStackLayoutVertical(0, 0,
                    panel.getListScrollView().getWidth());
            stackLayout.setMarginTop(4).setMarginBot(4).setPaddingTop(2).setMarginLeft(4)
                    .setMarginRight(4).setPaddingBot(2).setPaddingRight(2);
            stackLayout.doSetChildWidth(true);
            pData.getKnowledge().getTalentKnowledge().getKnownTrees().stream()
                    .map((loc) -> pData.getKnowledge().getTalentKnowledge().getTree(loc))
                    .sorted(Comparator.comparing((info) -> info.getTreeDefinition().getName()))
                    .forEach(record -> {
                        MKLayout talentEntry = new TalentListEntry(0, 0, 16, record, treeWidget, font, this);
                        stackLayout.addWidget(talentEntry);
                        MKRectangle div = new MKRectangle(0, 0,
                                panel.getListScrollView().getWidth() - 8, 1, 0x99ffffff);
                        stackLayout.addWidget(div);
                    });
            panel.setList(stackLayout);
            root.addWidget(panel);
        });
        return root;
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
        MKLayout root = getRootLayout(xPos, yPos, xOffset, dataBoxRegion.width);
        minecraft.player.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent((pData) -> {
            // Stat Panel
            int slotsY = yPos + DATA_BOX_OFFSET - 28;
            int slotsX = xPos + xOffset + 4;
            MKText activesLabel = new MKText(font, new TranslationTextComponent("mkcore.gui.actives"));
            activesLabel.setX(slotsX);
            activesLabel.setY(slotsY - 12);
            root.addWidget(activesLabel);
            MKLayout regularSlots = getLayoutOfAbilitySlots(slotsX, slotsY, MKAbility.AbilityType.Active
                    , GameConstants.MAX_ACTIVES);
            root.addWidget(regularSlots);
            regularSlots.manualRecompute();
            int ultSlotsX = regularSlots.getX() + regularSlots.getWidth() + 30;
            MKLayout ultSlots = getLayoutOfAbilitySlots(ultSlotsX, slotsY, MKAbility.AbilityType.Ultimate,
                    GameConstants.MAX_ULTIMATES);
            root.addWidget(ultSlots);
            ultSlots.manualRecompute();
            MKText ultLabel = new MKText(font, new TranslationTextComponent("mkcore.gui.ultimates"));
            ultLabel.setX(ultSlotsX);
            ultLabel.setY(slotsY - 12);
            root.addWidget(ultLabel);
            int passiveSlotX = ultSlots.getX() + ultSlots.getWidth() + 30;
            MKLayout passiveSlots = getLayoutOfAbilitySlots(passiveSlotX, slotsY, MKAbility.AbilityType.Passive,
                    GameConstants.MAX_PASSIVES);
            MKText passivesLabel = new MKText(font, new TranslationTextComponent("mkcore.gui.passives"));
            passivesLabel.setX(passiveSlotX);
            passivesLabel.setY(slotsY - 12);
            root.addWidget(passivesLabel);
            root.addWidget(passiveSlots);
            int contentX = xPos + xOffset;
            int contentY = yPos + DATA_BOX_OFFSET;
            int contentWidth = dataBoxRegion.width;
            int contentHeight = dataBoxRegion.height;
            ScrollingListPanelLayout panel = new ScrollingListPanelLayout(
                    contentX, contentY, contentWidth, contentHeight);
            currentScrollingPanel = panel;
            abilitiesScrollPanel = panel;
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
                        MKLayout abilityEntry = new AbilityListEntry(0, 0, 16, ability, infoWidget, font, this);
                        stackLayout.addWidget(abilityEntry);
                        MKRectangle div = new MKRectangle(0, 0,
                                panel.getListScrollView().getWidth() - 8, 1, 0x99ffffff);
                        stackLayout.addWidget(div);
                    });
            panel.setList(stackLayout);
            root.addWidget(panel);
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
            double baseValue = attribute.getBaseValue();
            if (attr.equals(SharedMonsterAttributes.ATTACK_SPEED) && minecraft.player != null){
                ItemStack itemInHand = minecraft.player.getHeldItemMainhand();
                if (!itemInHand.equals(ItemStack.EMPTY)){
                    if (itemInHand.getAttributeModifiers(EquipmentSlotType.MAINHAND).containsKey(attr.getName())){
                        Collection<AttributeModifier> itemAttackSpeed = itemInHand.getAttributeModifiers(EquipmentSlotType.MAINHAND)
                                .get(attr.getName());
                        double attackSpeed = 4.0;
                        for (AttributeModifier mod : itemAttackSpeed){
                            if (mod.getOperation().equals(AttributeModifier.Operation.ADDITION)){
                                attackSpeed += mod.getAmount();
                            }
                        }
                        baseValue = attackSpeed;
                    }
                }
            }
            if (attribute.getValue() < baseValue) {
                textWidget.setColor(NEGATIVE_COLOR);
            } else if (attribute.getValue() > baseValue) {
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
            abilitySlots.put(new AbilitySlotKey(slot.getSlotType(), slot.getSlotIndex()), slot);
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
    public void pushState(String newState) {
        super.pushState(newState);
        if (newState.equals("talents")){
            currentScrollingPanel = talentScrollPanel;
        } else if (newState.equals("abilities")){
            currentScrollingPanel = abilitiesScrollPanel;
        }
    }

    @Override
    public void setupScreen() {
        super.setupScreen();
        infoWidget = null;
        currentScrollingPanel = null;
        talentTreeWidget = null;
        addState("stats", () -> createScrollingPanelWithContent((pData, width) ->
                createStatList(pData, width, STAT_PANEL_ATTRIBUTES)));
        addState("damages", () -> createScrollingPanelWithContent(this::createDamageTypeList));
        addState("abilities", this::createAbilitiesPage);
        addState("talents", this::createTalentsPage);
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
        return handled;
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
    public void addRestoreStateCallbacks() {
        String state = getState();
        super.addRestoreStateCallbacks();
        if (state.equals("abilities")){
            final MKAbilityInfo abilityInf = getAbilityInfo();
            addPostSetupCallback(() -> {
                if (infoWidget != null){
                    infoWidget.setAbilityInfo(abilityInf);

                }
            });
        } else if (state.equals("talents")){
            final TalentTreeRecord current = getCurrentTree();
            addPostSetupCallback(() -> {
                if (talentTreeWidget != null){
                    talentTreeWidget.setTreeRecord(current);
                }
            });
        }
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

    public TalentTreeRecord getCurrentTree() {
        return currentTree;
    }

    public void setCurrentTree(TalentTreeRecord currentTree) {
        this.currentTree = currentTree;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
