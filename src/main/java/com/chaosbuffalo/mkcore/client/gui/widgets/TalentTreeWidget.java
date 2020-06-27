package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.client.gui.CharacterScreen;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.talents.TalentRecord;
import com.chaosbuffalo.mkcore.core.talents.TalentTreeDefinition;
import com.chaosbuffalo.mkcore.core.talents.TalentTreeRecord;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.TalentPointActionPacket;
import com.chaosbuffalo.mkwidgets.client.gui.UIConstants;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Arrays;
import java.util.Map;

public class TalentTreeWidget extends MKLayout {
    private final MKPlayerData playerData;
    private TalentTreeRecord treeRecord;
    private final FontRenderer fontRenderer;
    private final CharacterScreen screen;
    private int originalWidth;
    private int oringalHeight;

    public TalentTreeWidget(int x, int y, int width, int height,
                            MKPlayerData data, FontRenderer fontRenderer, CharacterScreen screen) {
        super(x, y, width, height);
        this.playerData = data;
        this.treeRecord = null;
        this.fontRenderer = fontRenderer;
        this.screen = screen;
        this.originalWidth = width;
        this.oringalHeight = height;
        setup();
    }

    public void setup(){
        if (treeRecord == null){
            MKText noSelectPrompt = new MKText(fontRenderer,
                    new TranslationTextComponent("mkcore.gui.select_talent_tree"));
            addWidget(noSelectPrompt);
            setWidth(originalWidth);
            setHeight(oringalHeight);
        } else {
            int treeRenderingMarginX = 10;
            int treeRenderingPaddingX = 10;
            int talentButtonHeight = TalentButton.HEIGHT;
            int talentButtonWidth = TalentButton.WIDTH;
            int talentButtonYMargin = 6;
            Map<String, TalentTreeDefinition.TalentLineDefinition> lineDefs = treeRecord
                    .getTreeDefinition().getTalentLines();
            int count =  lineDefs.size();
            int talentWidth = talentButtonWidth * count + treeRenderingMarginX * 2 + (count - 1) * treeRenderingPaddingX;
            int spacePerColumn = talentWidth / count;
            int columnOffset = (spacePerColumn - talentButtonWidth) / 2;
            int i = 0;
            String[] keys = lineDefs.keySet().toArray(new String[0]);
            Arrays.sort(keys);
            int largestIndex = 0;
            int columnOffsetTotal = 0;
            for (String name : keys) {
                TalentTreeDefinition.TalentLineDefinition lineDef = lineDefs.get(name);
                for (int talentIndex = 0; talentIndex < lineDef.getLength(); talentIndex++) {
                    TalentRecord record = treeRecord.getNodeRecord(name, talentIndex);
                    TalentButton button = new TalentButton(talentIndex, name, record,
                            getX() + spacePerColumn * i + columnOffsetTotal,
                            getY() + talentIndex * talentButtonHeight + talentButtonYMargin
                    );
                    button.setPressedCallback(this::pressTalentButton);
                    addWidget(button);
                    if (talentIndex > largestIndex) {
                        largestIndex = talentIndex;
                    }
                }
                i++;
                columnOffsetTotal += columnOffset;
            }
            setWidth(talentWidth);
            setHeight((largestIndex + 1) * talentButtonHeight + talentButtonYMargin);
        }
    }

    public Boolean pressTalentButton(MKButton button, Integer mouseButton) {
        TalentButton talentButton = (TalentButton) button;
        if (playerData != null) {
            if (mouseButton == UIConstants.MOUSE_BUTTON_RIGHT) {
                PacketHandler.sendMessageToServer(new TalentPointActionPacket(
                        treeRecord.getTreeDefinition().getTreeId(),
                        talentButton.line, talentButton.index,
                        TalentPointActionPacket.Action.REFUND));

            } else if (mouseButton == UIConstants.MOUSE_BUTTON_LEFT) {
                PacketHandler.sendMessageToServer(new TalentPointActionPacket(
                        treeRecord.getTreeDefinition().getTreeId(),
                        talentButton.line, talentButton.index,
                        TalentPointActionPacket.Action.SPEND));
            }
        }
        return true;
    }

    public void setTreeRecord(TalentTreeRecord treeRecord) {
        this.treeRecord = treeRecord;
        clearWidgets();
        setup();
    }
}
