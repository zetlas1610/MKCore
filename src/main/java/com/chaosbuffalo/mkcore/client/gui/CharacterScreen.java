package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.client.gui.widgets.*;
import com.chaosbuffalo.mkcore.core.AbilitySlotType;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.core.talents.TalentTreeRecord;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.LayoutRelativeWidthConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKRectangle;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKWidget;
import com.chaosbuffalo.mkwidgets.utils.TextureRegion;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CClientStatusPacket;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.*;
import java.util.stream.Collectors;

public class CharacterScreen extends AbilityPanelScreen {
    private TalentTreeWidget talentTreeWidget;
    private TalentTreeRecord currentTree;
    private ScrollingListPanelLayout talentScrollPanel;
    private static final ArrayList<IAttribute> STAT_PANEL_ATTRIBUTES = new ArrayList<>();

    public static class AbilitySlotKey {
        public AbilitySlotType type;
        public int slot;

        public AbilitySlotKey(AbilitySlotType type, int index) {
            this.type = type;
            this.slot = index;
        }

        @Override
        public int hashCode() {
            return slot + type.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof AbilitySlotKey) {
                AbilitySlotKey otherKey = (AbilitySlotKey) other;
                return slot == otherKey.slot && type.equals(otherKey.type);
            }
            return false;
        }
    }

    private final Map<AbilitySlotKey, AbilitySlotWidget> abilitySlots;

    public List<AbilitySlotWidget> getSlotsForType(AbilitySlotType slotType) {
        List<AbilitySlotWidget> widgets = new ArrayList<>();
        for (AbilitySlotWidget slot : abilitySlots.values()) {
            if (slot.getSlotType().equals(slotType)) {
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
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.BUFF_DURATION);
        STAT_PANEL_ATTRIBUTES.add(SharedMonsterAttributes.ATTACK_SPEED);
        STAT_PANEL_ATTRIBUTES.add(SharedMonsterAttributes.MOVEMENT_SPEED);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.COOLDOWN);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.CASTING_SPEED);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.MELEE_CRIT);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.MELEE_CRIT_MULTIPLIER);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.RANGED_CRIT);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.RANGED_CRIT_MULTIPLIER);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.SPELL_CRIT);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.SPELL_CRIT_MULTIPLIER);
    }

    public CharacterScreen() {
        super(new TranslationTextComponent("mk_character_screen.title"));
        abilitySlots = new HashMap<>();
        states.addAll(Arrays.asList("stats", "abilities", "talents", "damages"));
        setDoAbilityDrag(true);
    }

    private MKWidget createStatList(MKPlayerData pData, int panelWidth, List<IAttribute> toDisplay) {
        if (getMinecraft().player == null) {
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

    private MKWidget createTalentsPage() {
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        TextureRegion dataBoxRegion = GuiTextures.CORE_TEXTURES.getRegion(GuiTextures.DATA_BOX);
        if (minecraft == null || minecraft.player == null || dataBoxRegion == null) {
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
            MKStackLayoutHorizontal fieldTray = new MKStackLayoutHorizontal(contentX, contentY - 16, 12);
            fieldTray.setPaddingLeft(10);
            fieldTray.setPaddingRight(10);
            fieldTray.setMargins(10, 10, 0, 0);
            root.addWidget(fieldTray);
            NamedField totalTalents = new NamedField(0, 0, "Total Talents:",
                    0xff000000,
                    Integer.toString(pData.getKnowledge().getTalentKnowledge().getTotalTalentPoints()),
                    0xff000000, font);
            NamedField unspentTalents = new NamedField(0, 0, "Unspent Talents:", 0xff000000,
                    Integer.toString(pData.getKnowledge().getTalentKnowledge().getUnspentTalentPoints()),
                    0xff000000, font);
            fieldTray.addWidget(unspentTalents);
            fieldTray.addWidget(totalTalents);
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


    private MKWidget createAbilitiesPage() {
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        TextureRegion dataBoxRegion = GuiTextures.CORE_TEXTURES.getRegion(GuiTextures.DATA_BOX);
        if (minecraft == null || minecraft.player == null || dataBoxRegion == null) {
            return new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        }
        int xOffset = GuiTextures.CORE_TEXTURES.getCenterXOffset(
                GuiTextures.DATA_BOX, GuiTextures.BACKGROUND_320_240);
        MKLayout root = getRootLayout(xPos, yPos, xOffset, dataBoxRegion.width, true);
        minecraft.player.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent((pData) -> {
            // Stat Panel
            int slotsY = yPos + DATA_BOX_OFFSET - 28;
            int slotsX = xPos + xOffset + 4;
            MKText activesLabel = new MKText(font, new TranslationTextComponent("mkcore.gui.actives"));
            activesLabel.setX(slotsX);
            activesLabel.setY(slotsY - 12);
            root.addWidget(activesLabel);
            MKLayout regularSlots = getLayoutOfAbilitySlots(slotsX, slotsY, AbilitySlotType.Basic
                    , GameConstants.MAX_ACTIVES);
            root.addWidget(regularSlots);
            regularSlots.manualRecompute();
            int ultSlotsX = regularSlots.getX() + regularSlots.getWidth() + 30;
            MKLayout ultSlots = getLayoutOfAbilitySlots(ultSlotsX, slotsY, AbilitySlotType.Ultimate,
                    GameConstants.MAX_ULTIMATES);
            root.addWidget(ultSlots);
            ultSlots.manualRecompute();
            MKText ultLabel = new MKText(font, new TranslationTextComponent("mkcore.gui.ultimates"));
            ultLabel.setX(ultSlotsX);
            ultLabel.setY(slotsY - 12);
            root.addWidget(ultLabel);
            int passiveSlotX = ultSlots.getX() + ultSlots.getWidth() + 30;
            MKLayout passiveSlots = getLayoutOfAbilitySlots(passiveSlotX, slotsY, AbilitySlotType.Passive,
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
            ScrollingListPanelLayout panel = getAbilityScrollPanel(contentX, contentY,
                    contentWidth, contentHeight, pData, pData.getKnowledge().getKnownAbilities()
                            .getKnownStream().map(MKAbilityInfo::getAbility).collect(Collectors.toList()));
            currentScrollingPanel = panel;
            abilitiesScrollPanel = panel;
            root.addWidget(panel);
        });
        return root;
    }

    private MKWidget createDamageTypeList(MKPlayerData pData, int panelWidth) {
        AbstractAttributeMap attributes = getMinecraft().player.getAttributes();
        MKStackLayoutVertical stackLayout = new MKStackLayoutVertical(0, 0, panelWidth);
        stackLayout.setMarginTop(4).setMarginBot(4).setPaddingTop(2).setMarginLeft(4)
                .setMarginRight(4).setPaddingBot(2);
        stackLayout.doSetChildWidth(false);
        List<MKDamageType> damageTypes = new ArrayList<>(MKCoreRegistry.DAMAGE_TYPES.getValues());
        damageTypes.sort(Comparator.comparing(MKDamageType::getDisplayName));
        for (MKDamageType damageType : damageTypes) {
            if (damageType.shouldDisplay()) {
                IconText iconText = new IconText(0, 0, 16,
                        damageType.getDisplayName(), damageType.getIcon(), font, 16, 2);
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
            if (attr.equals(SharedMonsterAttributes.ATTACK_SPEED) && minecraft.player != null) {
                ItemStack itemInHand = minecraft.player.getHeldItemMainhand();
                if (!itemInHand.equals(ItemStack.EMPTY)) {
                    if (itemInHand.getAttributeModifiers(EquipmentSlotType.MAINHAND).containsKey(attr.getName())) {
                        Collection<AttributeModifier> itemAttackSpeed = itemInHand.getAttributeModifiers(EquipmentSlotType.MAINHAND)
                                .get(attr.getName());
                        double attackSpeed = 4.0;
                        for (AttributeModifier mod : itemAttackSpeed) {
                            if (mod.getOperation().equals(AttributeModifier.Operation.ADDITION)) {
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

    private MKLayout getLayoutOfAbilitySlots(int x, int y, AbilitySlotType slotType, int count) {
        MKStackLayoutHorizontal layout = new MKStackLayoutHorizontal(x, y, 24);
        layout.setPaddings(2, 2, 0, 0);
        layout.setMargins(2, 2, 2, 2);
        for (int i = 0; i < count; i++) {
            AbilitySlotWidget slot = new AbilitySlotWidget(0, 0, slotType, i, this);
            abilitySlots.put(new AbilitySlotKey(slot.getSlotType(), slot.getSlotIndex()), slot);
            layout.addWidget(slot);
        }
        return layout;
    }


    @Override
    public void pushState(String newState) {
        super.pushState(newState);
        if (newState.equals("talents")) {
            currentScrollingPanel = talentScrollPanel;
        } else if (newState.equals("abilities")) {
            currentScrollingPanel = abilitiesScrollPanel;
        }
    }

    @Override
    protected void init() {
        super.init();
        if (minecraft != null && minecraft.getConnection() != null){
            minecraft.getConnection().sendPacket(new CClientStatusPacket(CClientStatusPacket.State.REQUEST_STATS));
        }
    }

    private void addStatTextToLayout(MKLayout layout, ResourceLocation statName,
                                     ClientPlayerEntity clientPlayer){
        Stat<ResourceLocation> statType = Stats.CUSTOM.get(statName);
        String formattedValue = statType.format(clientPlayer.getStats().getValue(Stats.CUSTOM, statName));
        TranslationTextComponent statNameTranslated = new TranslationTextComponent("stat." +
                statType.getValue().toString().replace(':', '.'));
        MKText statText = new MKText(font, String.format("%s: %s", statNameTranslated.getFormattedText(), formattedValue));
        layout.addWidget(statText);
        addPreDrawRunnable(() -> {
            String val = statType.format(clientPlayer.getStats().getValue(Stats.CUSTOM, statName));
            statText.setText(String.format("%s: %s", statNameTranslated.getFormattedText(), val));
        });
    }

    public void setupDamageHeader(MKPlayerData playerData, MKLayout layout){
        MKStackLayoutVertical stackLayout = new MKStackLayoutVertical(0, 0, layout.getWidth());
        layout.addConstraintToWidget(new MarginConstraint(MarginConstraint.MarginType.LEFT), stackLayout);
        layout.addConstraintToWidget(new MarginConstraint(MarginConstraint.MarginType.TOP), stackLayout);
        layout.addConstraintToWidget(new LayoutRelativeWidthConstraint(1.0f), stackLayout);
        stackLayout.setMargins(4, 4, 4, 4);
        stackLayout.setPaddingTop(2);
        stackLayout.setPaddingBot(2);
        layout.addWidget(stackLayout);
        ClientPlayerEntity clientPlayer = Minecraft.getInstance().player;
        if (clientPlayer != null){
            addStatTextToLayout(stackLayout, Stats.DAMAGE_DEALT, clientPlayer);
            addStatTextToLayout(stackLayout, Stats.DAMAGE_TAKEN, clientPlayer);
            addStatTextToLayout(stackLayout, Stats.DAMAGE_RESISTED, clientPlayer);
        }


    }

    public void setupStatsHeader(MKPlayerData playerData, MKLayout layout){
        MKStackLayoutVertical stackLayout = new MKStackLayoutVertical(0, 0, layout.getWidth());
        layout.addConstraintToWidget(new MarginConstraint(MarginConstraint.MarginType.LEFT), stackLayout);
        layout.addConstraintToWidget(new MarginConstraint(MarginConstraint.MarginType.TOP), stackLayout);
        layout.addConstraintToWidget(new LayoutRelativeWidthConstraint(1.0f), stackLayout);
        stackLayout.setMargins(4, 4, 4, 4);
        stackLayout.setPaddingTop(2);
        stackLayout.setPaddingBot(2);
        layout.addWidget(stackLayout);
        String personaNameText = I18n.format("mkcore.gui.character.persona_name",
                playerData.getPersonaManager().getActivePersona().getName());
        MKText personaName = new MKText(font, personaNameText);
        stackLayout.addWidget(personaName);
        String healthText = I18n.format("mkcore.gui.character.current_health",
                String.format("%.0f", playerData.getStats().getHealth()),
                String.format("%.0f", playerData.getStats().getMaxHealth()));
        MKText health = new MKText(font, healthText);
        String manaText = I18n.format("mkcore.gui.character.current_mana",
                String.format("%.0f", playerData.getStats().getMana()),
                String.format("%.0f", playerData.getStats().getMaxMana()));
        MKText mana = new MKText(font, manaText);
        addPreDrawRunnable(() -> {
            mana.setText(I18n.format("mkcore.gui.character.current_mana",
                    String.format("%.0f", playerData.getStats().getMana()),
                    String.format("%.0f", playerData.getStats().getMaxMana())));
        });
        addPreDrawRunnable(() -> {
            health.setText(I18n.format("mkcore.gui.character.current_health",
                    String.format("%.0f", playerData.getStats().getHealth()),
                    String.format("%.0f", playerData.getStats().getMaxHealth())));
        });
        stackLayout.addWidget(health);
        stackLayout.addWidget(mana);
    }

    @Override
    public void setupScreen() {
        super.setupScreen();
        infoWidget = null;
        currentScrollingPanel = null;
        talentTreeWidget = null;
        addState("stats", () -> createScrollingPanelWithContent((pData, width) ->
                createStatList(pData, width, STAT_PANEL_ATTRIBUTES), this::setupStatsHeader));
        addState("damages", () -> createScrollingPanelWithContent(this::createDamageTypeList,
                this::setupDamageHeader));
        addState("abilities", this::createAbilitiesPage);
        addState("talents", this::createTalentsPage);
        pushState("stats");
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        boolean handled = super.mouseReleased(mouseX, mouseY, mouseButton);
        if (isDraggingAbility) {
            clearDragging();
            clearDragState();
            return true;
        }
        return handled;
    }

    @Override
    public void clearDragging() {
        for (AbilitySlotWidget widget : abilitySlots.values()) {
            widget.setBackgroundColor(0xffffffff);
            widget.setIconColor(0xffffffff);
        }
        super.clearDragging();
    }

    @Override
    public void setDragging(MKAbility dragging) {
        super.setDragging(dragging);
        Arrays.stream(AbilitySlotType.values())
                .filter(type -> type != dragging.getType().getSlotType())
                .forEach(type -> {
                    for (AbilitySlotWidget widget : getSlotsForType(type)) {
                        widget.setBackgroundColor(0xff555555);
                        widget.setIconColor(0xff555555);
                    }
                });
    }

    @Override
    public void addRestoreStateCallbacks() {
        String state = getState();
        super.addRestoreStateCallbacks();
        if (state.equals("abilities")) {
            final MKAbility abilityInf = getAbility();
            addPostSetupCallback(() -> {
                if (infoWidget != null) {
                    infoWidget.setAbility(abilityInf);
                }
            });
        } else if (state.equals("talents")) {
            final TalentTreeRecord current = getCurrentTree();
            addPostSetupCallback(() -> {
                if (talentTreeWidget != null) {
                    talentTreeWidget.setTreeRecord(current);
                }
            });
        }
        restoreScrollingPanelState();
    }

    public TalentTreeRecord getCurrentTree() {
        return currentTree;
    }

    public void setCurrentTree(TalentTreeRecord currentTree) {
        this.currentTree = currentTree;
    }

}
