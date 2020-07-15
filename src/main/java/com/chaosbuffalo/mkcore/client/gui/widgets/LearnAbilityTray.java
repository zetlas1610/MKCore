package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.training.AbilityRequirementEvaluation;
import com.chaosbuffalo.mkcore.client.gui.GuiTextures;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerLearnAbilityRequestPacket;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.LayoutRelativeWidthConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.instructions.HoveringTextInstruction;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.math.Vec2i;
import com.chaosbuffalo.mkwidgets.client.gui.screens.IMKScreen;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LearnAbilityTray extends MKStackLayoutVertical {
    private MKAbility ability;
    private List<AbilityRequirementEvaluation> unmetRequirements;
    private final MKPlayerData playerData;
    private final FontRenderer font;
    protected final int POPUP_WIDTH = 180;
    protected final int POPUP_HEIGHT = 201;
    private final int trainerEntityId;
    private MKModal choosePoolSlotWidget;

    public LearnAbilityTray(int x, int y, int width, MKPlayerData playerData, FontRenderer font, int trainerEntityId) {
        super(x, y, width);
        this.playerData = playerData;
        this.trainerEntityId = trainerEntityId;
        unmetRequirements = new ArrayList<>();
        this.font = font;
        this.ability = null;
        setMarginTop(2);
        setMarginBot(2);
        setPaddingTop(2);
        setPaddingBot(2);
        setup();
    }

    private MKModal getChoosePoolSlotWidget(){
        if (getScreen() == null){
            return null;
        }
        IMKScreen screen = getScreen();
        MKModal popup = new MKModal();
        int screenWidth = screen.getWidth();
        int screenHeight = screen.getHeight();
        int xPos = (screenWidth - POPUP_WIDTH) / 2;
        int yPos = (screenHeight - POPUP_HEIGHT) / 2;
        MKImage background = GuiTextures.CORE_TEXTURES.getImageForRegion(
                GuiTextures.BACKGROUND_180_200, xPos, yPos, POPUP_WIDTH, POPUP_HEIGHT);
        popup.addWidget(background);
        String promptText = I18n.format("mkcore.gui.character.forget_ability",
                getAbility().getAbilityName());
        MKText prompt = new MKText(font, promptText, xPos + 6, yPos + 6);
        prompt.setWidth(POPUP_WIDTH - 10);
        prompt.setMultiline(true);
        popup.addWidget(prompt);
        MKScrollView scrollview = new MKScrollView(xPos + 15, yPos + 40, POPUP_WIDTH - 30,
                POPUP_HEIGHT - 45, true);
        popup.addWidget(scrollview);
        MKStackLayoutVertical abilities = new MKStackLayoutVertical(0, 0, scrollview.getWidth());
        abilities.setPaddingBot(2);
        abilities.setPaddingTop(2);
        abilities.setMargins(2, 2, 2, 2);
        abilities.doSetChildWidth(true);
        List<ResourceLocation> abilitySlots = playerData.getKnowledge().getKnownAbilities().getAbilityPool();
        for (ResourceLocation loc : abilitySlots){
            if (loc.equals(MKCoreRegistry.INVALID_ABILITY)){
                continue;
            }
            MKAbility ability = MKCoreRegistry.getAbility(loc);
            if (ability != null){
                AbilityForgetOption abilityIcon = new AbilityForgetOption(ability, abilitySlots,
                        getAbility().getAbilityId(), popup, font, trainerEntityId);
                abilities.addWidget(abilityIcon);
            }
        }
        scrollview.addWidget(abilities);
        abilities.manualRecompute();
        scrollview.setToRight();
        scrollview.setToTop();

        return popup;
    }

    public void setup() {
        clearWidgets();
        choosePoolSlotWidget = null;
        if (getAbility() != null) {
            choosePoolSlotWidget = getChoosePoolSlotWidget();
            MKStackLayoutHorizontal nameTray = new MKStackLayoutHorizontal(0, 0, 20);
            nameTray.setPaddingRight(4);
            nameTray.setPaddingLeft(4);
            IconText abilityName = new IconText(0, 0, 16, getAbility().getAbilityName(),
                    getAbility().getAbilityIcon(), font, 16, 1);
            nameTray.addWidget(abilityName);
            addWidget(nameTray);
            boolean isKnown = playerData.getKnowledge().getKnownAbilityInfo(getAbility().getAbilityId()) != null;
            boolean canLearn = unmetRequirements.stream().allMatch(x -> x.isMet);
            String knowText;
            if (isKnown) {
                knowText = I18n.format("mkcore.gui.character.already_known");
            } else if (!canLearn) {
                knowText = I18n.format("mkcore.gui.character.unmet_req");
            } else {
                knowText = I18n.format("mkcore.gui.character.can_learn");
            }
            MKText doesKnowWid = new MKText(font, knowText);
            doesKnowWid.setWidth(font.getStringWidth(knowText));
            addWidget(doesKnowWid);
            MKScrollView reqScrollView = new MKScrollView(0, 0, getWidth(), 40,
                    true);
            MKStackLayoutVertical reqlayout = new MKStackLayoutVertical(0, 0, getWidth());
            reqlayout.setPaddingBot(1);
            reqlayout.setPaddingTop(1);
            reqScrollView.addWidget(reqlayout);
            ArrayList<String> texts = unmetRequirements.stream()
                    .map((x) -> new StringTextComponent(
                            String.format("  - %s", x.requirementDescription.getFormattedText()))
                            .applyTextStyle(x.isMet ? TextFormatting.GREEN : TextFormatting.BLACK).getFormattedText())
                    .collect(Collectors.toCollection(ArrayList::new));
            for (String text : texts) {
                MKText reqText = new MKText(font, text);
                reqText.setMultiline(true);
                reqlayout.addConstraintToWidget(new LayoutRelativeWidthConstraint(1.0f), reqText);
                reqlayout.addWidget(reqText);
            }
            reqlayout.manualRecompute();
            addWidget(reqScrollView);
            manualRecompute();
            reqScrollView.setToTop();
            reqScrollView.setToRight();
            if (!isKnown){
                String learnButtonText = I18n.format("mkcore.gui.character.learn");
                MKButton learnButton = new MKButton(0, 0, learnButtonText) {

                    @Override
                    public boolean checkHovered(int mouseX, int mouseY) {
                        return this.isVisible() && this.isInBounds(mouseX, mouseY);
                    }

                    @Override
                    public void onMouseHover(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
                        super.onMouseHover(mc, mouseX, mouseY, partialTicks);
                        if (unmetRequirements.size() > 0) {
                            if (getScreen() != null) {
                                getScreen().addPostRenderInstruction(new HoveringTextInstruction(
                                        I18n.format("mkcore.gui.character.unmet_req_tooltip"),
                                        getParentCoords(new Vec2i(mouseX, mouseY))));
                            }
                        }
                    }
                };
                learnButton.setWidth(font.getStringWidth(learnButtonText) + 10);
                learnButton.setEnabled(canLearn);
                learnButton.setPressedCallback((button, buttonType) -> {
                    if (getAbility().requiresSlot()){
                        if (!playerData.getKnowledge()
                                .getKnownAbilities().isAbilityPoolFull()){
                            MKCore.LOGGER.info("Ability pool {}",
                                    playerData.getKnowledge().getKnownAbilities().getCurrentPoolCount());
                            PacketHandler.sendMessageToServer(new PlayerLearnAbilityRequestPacket(
                                    getAbility().getAbilityId(), playerData.getKnowledge()
                                    .getKnownAbilities().getCurrentPoolCount(), trainerEntityId));
                        } else {
                            MKCore.LOGGER.info("Ability pool full {} ",
                                    playerData.getKnowledge().getKnownAbilities().getCurrentPoolCount());
                            if (getScreen() != null && choosePoolSlotWidget != null){
                                getScreen().addModal(choosePoolSlotWidget);
                            }
                        }
                    } else {
                        PacketHandler.sendMessageToServer(new PlayerLearnAbilityRequestPacket(
                                getAbility().getAbilityId(), trainerEntityId));
                    }
                    return true;
                });
                nameTray.addWidget(learnButton);
            }
        } else {
            MKText prompt = new MKText(font, I18n.format("mkcore.gui.character.learn_ability_prompt"));
            addWidget(prompt);
        }
    }

    public void setAbility(MKAbility ability, List<AbilityRequirementEvaluation> requirements) {
        this.ability = ability;
        this.unmetRequirements = requirements;
        setup();
    }

    public MKAbility getAbility() {
        return ability;
    }

}
