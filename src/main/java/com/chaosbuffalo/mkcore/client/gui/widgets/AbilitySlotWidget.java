package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.client.gui.CharacterScreen;
import com.chaosbuffalo.mkcore.client.gui.GuiTextures;
import com.chaosbuffalo.mkcore.core.AbilitySlot;
import com.chaosbuffalo.mkcore.core.IActiveAbilityContainer;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerSlotAbilityPacket;
import com.chaosbuffalo.mkwidgets.client.gui.UIConstants;
import com.chaosbuffalo.mkwidgets.client.gui.actions.IDragState;
import com.chaosbuffalo.mkwidgets.client.gui.actions.WidgetHoldingDragState;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.FillConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.instructions.HoveringTextInstruction;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.math.IntColor;
import com.chaosbuffalo.mkwidgets.client.gui.math.Vec2i;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKImage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

public class AbilitySlotWidget extends MKLayout {
    private AbilitySlot slotType;
    private boolean unlocked;
    private final int slotIndex;
    private CharacterScreen screen;
    private ResourceLocation abilityId;
    private MKImage background;
    private MKImage icon;

    public AbilitySlotWidget(int x, int y, AbilitySlot slotType, int slotIndex, CharacterScreen screen) {
        super(x, y, 20, 20);
        this.slotType = slotType;
        this.screen = screen;
        this.slotIndex = slotIndex;
        this.setMargins(2, 2, 2, 2);
        this.abilityId = MKCoreRegistry.INVALID_ABILITY;
        this.icon = null;
        refreshSlot();
    }

    public void setIconColor(int color) {
        if (icon != null) {
            icon.setColor(new IntColor(color));
        }
    }

    public void setBackgroundColor(int color) {
        background.setColor(new IntColor(color));
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public AbilitySlot getSlotType() {
        return slotType;
    }

    private void refreshSlot() {
        PlayerEntity playerEntity = Minecraft.getInstance().player;
        if (playerEntity == null)
            return;
        MKCore.getPlayer(playerEntity).ifPresent((playerData -> {
            abilityId = playerData.getKnowledge().getAbilityInSlot(slotType, slotIndex);
            setupBackground(playerData);
            setupIcon(abilityId);
        }));
    }

    private void setupBackground(MKPlayerData playerData) {
        if (background != null) {
            removeWidget(background);
        }
        IActiveAbilityContainer container = playerData.getKnowledge().getAbilityContainer(slotType);
        unlocked = container.isSlotUnlocked(slotIndex);
        background = getImageForSlotType(slotType, unlocked);
        addWidget(background);
        addConstraintToWidget(new FillConstraint(), background);
    }

    private void setupIcon(ResourceLocation abilityName) {
        if (icon != null) {
            removeWidget(icon);
        }
        if (!this.abilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
            MKAbility ability = MKCoreRegistry.getAbility(abilityName);
            if (ability != null) {
                MKCore.LOGGER.info("Adding icon to slot {} {}", ability.getAbilityIcon(), slotIndex);
                icon = new MKImage(0, 0, 16, 16, ability.getAbilityIcon());
                addWidget(icon);
                addConstraintToWidget(new MarginConstraint(MarginConstraint.MarginType.TOP), icon);
                addConstraintToWidget(new MarginConstraint(MarginConstraint.MarginType.LEFT), icon);
            }
        }
    }

    private boolean getUnlocked(MKAbility.AbilityType slotType, int slotIndex) {
        switch (slotType) {
            case Ultimate:
                return slotIndex < GameConstants.DEFAULT_ULTIMATES;
            case Passive:
                return slotIndex < GameConstants.DEFAULT_PASSIVES;
            default:
                return slotIndex < GameConstants.DEFAULT_ACTIVES;
        }
    }

    private MKImage getImageForSlotType(AbilitySlot slotType, boolean unlocked) {
        switch (slotType) {
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

    public ResourceLocation getAbilityId() {
        return abilityId;
    }

    private void setSlotToAbility(ResourceLocation ability) {
        PacketHandler.sendMessageToServer(new PlayerSlotAbilityPacket(slotType, slotIndex, ability));
    }

    @Override
    public void onDragEnd(IDragState state) {
        icon.setColor(new IntColor(0xffffffff));
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == UIConstants.MOUSE_BUTTON_LEFT) {
            if (!(abilityId.equals(MKCoreRegistry.INVALID_ABILITY))) {
                MKAbility ability = MKCoreRegistry.getAbility(getAbilityId());
                if (ability == null) {
                    return false;
                }
                screen.setDragState(new WidgetHoldingDragState(new MKImage(0, 0, icon.getWidth(),
                        icon.getHeight(), icon.getImageLoc())), this);
                screen.setDragging(ability);
                icon.setColor(new IntColor(0xff555555));
                return true;
            }
        } else if (mouseButton == UIConstants.MOUSE_BUTTON_RIGHT) {
            setSlotToAbility(MKCoreRegistry.INVALID_ABILITY);
            return true;
        }
        return false;
    }

    @Override
    public boolean onMouseRelease(double mouseX, double mouseY, int mouseButton) {
        if (screen.isDraggingAbility()) {
            MKCore.LOGGER.info("adding ability {} to slot {} {} {} {}", screen.getDragging(), slotIndex, unlocked, screen.getDragging().getType(), slotType);
            if (unlocked && screen.getDragging().getType().fitsSlot(slotType)) {
                ResourceLocation ability = screen.getDragging().getAbilityId();
                setSlotToAbility(ability);
            }
            screen.clearDragging();
            screen.clearDragState();
            return true;
        }
        return false;
    }

    @Override
    public void postDraw(Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        if (isHovered()){
            if (getScreen() != null){
                if (!getAbilityId().equals(MKCoreRegistry.INVALID_ABILITY)){
                    MKAbility ability = MKCoreRegistry.getAbility(getAbilityId());
                    if (ability != null){
                        getScreen().addPostRenderInstruction(new HoveringTextInstruction(
                                ability.getAbilityName(),
                                getParentCoords(new Vec2i(mouseX, mouseY))));
                    }
                }

            }

        }
    }
}
