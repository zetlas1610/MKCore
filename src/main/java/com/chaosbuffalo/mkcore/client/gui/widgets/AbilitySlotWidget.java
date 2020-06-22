package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.client.gui.CharacterScreen;
import com.chaosbuffalo.mkcore.client.gui.GuiTextures;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.LayoutRelativeHeightConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.LayoutRelativeWidthConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.LayoutRelativeXPosConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.LayoutRelativeYPosConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKImage;

public class AbilitySlotWidget extends MKLayout {
    private MKAbility.AbilityType slotType;
    private boolean unlocked;
    private final int slotIndex;
    private CharacterScreen screen;

    public AbilitySlotWidget(int x, int y, MKAbility.AbilityType slotType, int slotIndex, boolean unlocked, CharacterScreen screen) {
        super(x, y, 20, 20);
        this.slotType = slotType;
        this.unlocked = unlocked;
        this.screen = screen;
        this.slotIndex = slotIndex;
        MKImage background = getImageForSlotType(slotType, unlocked);
        addWidget(background);
        addConstraintToWidget(new LayoutRelativeWidthConstraint(1.0f), background);
        addConstraintToWidget(new LayoutRelativeHeightConstraint(1.0f), background);
        addConstraintToWidget(new LayoutRelativeXPosConstraint(0.0f), background);
        addConstraintToWidget(new LayoutRelativeYPosConstraint(0.0f), background);
    }

    private MKImage getImageForSlotType(MKAbility.AbilityType slotType, boolean unlocked){
        switch (slotType){
            case Ultimate:
                return GuiTextures.CORE_TEXTURES.getImageForRegion(unlocked ?
                                GuiTextures.ABILITY_SLOT_ULT : GuiTextures.ABILITY_SLOT_ULT_LOCKED,
                        getX(), getY(), getWidth(), getHeight());
            case Passive:
                return GuiTextures.CORE_TEXTURES.getImageForRegion(unlocked ?
                                GuiTextures.ABILITY_SLOT_PASSIVE : GuiTextures.ABILITY_SLOT_PASSIVE_LOCKED,
                        getX(), getY(), getWidth(), getHeight());
            default:
                return GuiTextures.CORE_TEXTURES.getImageForRegion(unlocked ?
                                GuiTextures.ABILITY_SLOT_REG : GuiTextures.ABILITY_SLOT_REG_LOCKED,
                        getX(), getY(), getWidth(), getHeight());
        }
    }

    @Override
    public boolean onMouseRelease(double mouseX, double mouseY, int mouseButton) {
        if (screen.isDraggingAbility()){
            MKCore.LOGGER.info("adding ability {} to slot {}", screen.getDragging(), slotIndex);
            screen.clearDragging();
            screen.clearDragState();
            return true;
        }
        return false;
    }
}
