package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.Capabilities;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.screens.MKScreen;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKWidget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;

public class CharacterScreen extends MKScreen {
    private final int PANEL_WIDTH = 320;
    private final int PANEL_HEIGHT = 240;
    private final int DATA_BOX_OFFSET = 78;
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
    }

    private MKWidget drawStatPanel(MKPlayerData pData, int xPos, int yPos, int panelWidth) {
        if (getMinecraft().player == null){
            return null;
        }
        AbstractAttributeMap attributes = getMinecraft().player.getAttributes();
        MKStackLayoutVertical stackLayout = new MKStackLayoutVertical(xPos, yPos, panelWidth);
        stackLayout.setMarginTop(4).setMarginBot(4).setPaddingTop(2).setMarginLeft(4)
                .setMarginRight(4).setPaddingBot(2);
        stackLayout.doSetChildWidth(true);


//        MKText talentPoints = new MKText(mc.fontRenderer,
//                String.format("%s: %d/%d",
//                        I18n.format("mkcore.ui_msg.talent_points"),
//                        pData.getUnspentTalentPoints(), pData.getTotalTalentPoints()));
//        talentPoints.setColor(16777215);
//        String armorClassString = String.format("%s: %s",
//                I18n.format("mkcore.ui_msg.armor_class"),
//                pData.getArmorClass().getName());
//        MKText armorClass = new MKText(mc.fontRenderer, armorClassString);
//        armorClass.setColor(16777215);
//        stackLayout.addWidget(level);
//        stackLayout.addWidget(unspentPoints);
//        stackLayout.addWidget(talentPoints);
//        stackLayout.addWidget(armorClass);
//        addPreDrawRunnable(() -> {
//            talentPoints.setText(String.format("%s: %d/%d",
//                    I18n.format("mkultra.ui_msg.talent_points"),
//                    pData.getUnspentTalentPoints(), pData.getTotalTalentPoints()));
//            unspentPoints.setText(String.format("%s: %d/%d",
//                    I18n.format("mkultra.ui_msg.ability_points"),
//                    pData.getUnspentPoints(), pData.getLevel()));
//            level.setText(String.format("%s: %d", I18n.format("mkultra.ui_msg.level"),
//                    pData.getLevel()));
//            armorClass.setText(String.format("%s: %s",
//                    I18n.format("mkultra.ui_msg.armor_class"),
//                    pData.getArmorClass().getName()));
//        });

        for (IAttribute attr : STAT_PANEL_ATTRIBUTES) {
            IAttributeInstance attribute = attributes.getAttributeInstance(attr);
            String text = String.format("%s: %.2f", I18n.format(String.format("attribute.name.%s",
                    attribute.getAttribute().getName())), attribute.getValue());
            MKText textWidget = new MKText(minecraft.fontRenderer, text).setMultiline(true);
            addPreDrawRunnable(() -> {
                String newText = String.format("%s: %.2f", I18n.format(String.format("attribute.name.%s",
                        attribute.getAttribute().getName())), attribute.getValue());
                textWidget.setText(newText);
                if (attribute.getValue() < attribute.getBaseValue()) {
                    textWidget.setColor(13111115);
                } else if (attribute.getValue() > attribute.getBaseValue()) {
                    textWidget.setColor(3334475);
                } else {
                    textWidget.setColor(16777215);
                }
            });
            stackLayout.addWidget(textWidget);
        }
        return stackLayout;
    }

    public MKLayout createStatPanel(){
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        MKLayout root = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        root.setMargins(5, 5, 5, 5);
        root.setPaddingTop(5).setPaddingBot(5);

        int xOffset = GuiTextures.CORE_TEXTURES.getCenterXOffset(GuiTextures.DATA_BOX, GuiTextures.BACKGROUND_320_240);

        if (minecraft == null || minecraft.player == null){
            return root;
        }
        minecraft.player.getCapability(Capabilities.PLAYER_CAPABILITY).ifPresent((pData) -> {
            // Stat Panel
            ManualAtlas.TextureRegion dataBoxRegion = GuiTextures.CORE_TEXTURES.getRegion(GuiTextures.DATA_BOX);
            if (dataBoxRegion != null){
                MKScrollView statScrollView = new MKScrollView(xPos + xOffset + 4,
                        yPos + DATA_BOX_OFFSET + 4,
                        dataBoxRegion.width - 8, dataBoxRegion.height - 8, true);
                statScrollView.addWidget(drawStatPanel(pData, 0, 0, dataBoxRegion.width - 8));
                statScrollView.setToTop();
                statScrollView.setToRight();
                root.addWidget(statScrollView);
            }
        });

        return root;
    }

    @Override
    public void setupScreen() {
        super.setupScreen();
        addState("stats", this::createStatPanel);
        pushState("stats");

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
