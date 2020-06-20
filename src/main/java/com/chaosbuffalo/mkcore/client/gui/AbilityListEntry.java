package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterYConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKImage;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class AbilityListEntry extends MKStackLayoutHorizontal {
    private final MKAbilityInfo info;
    private final AbilityInfoWidget infoWidget;

    public AbilityListEntry(int x, int y, int height, MKAbilityInfo info, AbilityInfoWidget infoWidget,
                            FontRenderer font) {
        super(x, y, height);
        this.info = info;
        this.infoWidget = infoWidget;
        setPaddingRight(2);
        setPaddingLeft(2);
        MKImage icon = new MKImage(0, 0, 16, 16, info.getAbility().getAbilityIcon());
        addWidget(icon);
        MKText name = new MKText(font, info.getAbility().getAbilityName());
        name.setWidth(100);
        name.setColor(0xffffffff);
        addWidget(name);
        addConstraintToWidget(new CenterYConstraint(), name);
    }

    @Override
    public void postDraw(Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        if (isHovered() || info.equals(infoWidget.getAbilityInfo())){
            mkFill(x, y, x + width, y + height, 0x55ffffff);
        }
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        MKCore.LOGGER.info("On mouse press: {}", info.getAbility().getAbilityId());
        infoWidget.setAbilityInfo(info);
        return true;
    }
}
