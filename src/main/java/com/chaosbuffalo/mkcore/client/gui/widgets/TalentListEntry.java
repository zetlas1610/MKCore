package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.client.gui.CharacterScreen;
import com.chaosbuffalo.mkcore.client.gui.constraints.CenterYWithOffsetConstraint;
import com.chaosbuffalo.mkcore.core.talents.TalentTreeRecord;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterYConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class TalentListEntry extends MKStackLayoutHorizontal {

    private final TalentTreeWidget treeWidget;
    private final TalentTreeRecord record;
    private final FontRenderer font;
    private final CharacterScreen screen;

    public TalentListEntry(int x, int y, int height, TalentTreeRecord record,
                           TalentTreeWidget treeWidget, FontRenderer font, CharacterScreen screen) {
        super(x, y, height);
        this.treeWidget = treeWidget;
        this.record = record;
        this.font = font;
        this.screen = screen;
        setPaddingRight(2);
        setPaddingLeft(2);
        setMarginLeft(6);
        MKText name = new MKText(font, record.getTreeDefinition().getName());
        name.setWidth(100);
        name.setColor(0xffffffff);
        addWidget(name);
        addConstraintToWidget(new CenterYWithOffsetConstraint(1), name);
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        treeWidget.setTreeRecord(record);
        screen.setCurrentTree(record);
        return true;
    }

    @Override
    public void postDraw(Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        if (isHovered()){
            mkFill(x, y, x + width, y + height, 0x55ffffff);
        }
        if (record.equals(screen.getCurrentTree())){
            mkFill(x, y, x + width, y + height, 0x99ffffff);
        }
    }
}
