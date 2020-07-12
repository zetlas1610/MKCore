package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.training.AbilityRequirementEvaluation;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerLearnAbilityRequestPacket;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.LayoutRelativeWidthConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.instructions.HoveringTextInstruction;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.math.Vec2i;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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
    private final int trainerEntityId;

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

    public void setup() {
        clearWidgets();
        if (getAbility() != null) {
            MKStackLayoutHorizontal nameTray = new MKStackLayoutHorizontal(0, 0, 20);
            nameTray.setPaddingRight(4);
            nameTray.setPaddingLeft(4);
            IconText abilityName = new IconText(0, 0, 16, getAbility().getAbilityName(),
                    getAbility().getAbilityIcon(), font, 16, 1);
            nameTray.addWidget(abilityName);
            addWidget(nameTray);
            boolean isKnown = playerData.getKnowledge().getKnownAbilityInfo(getAbility().getAbilityId()) != null;
            boolean canLearn = unmetRequirements.isEmpty() && !isKnown;
            String knowText;
            if (isKnown) {
                knowText = "already known";
            } else if (unmetRequirements.size() > 0) {
                knowText = "unmet req";
            } else {
                knowText = "can learn";
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
            for (String text : texts){
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
                MKButton learnButton = new MKButton(0, 0, "Learn") {

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
                                        "You do not meet the requirements to learn this ability.",
                                        getParentCoords(new Vec2i(mouseX, mouseY))));
                            }
                        }
                    }
                };
                learnButton.setWidth(font.getStringWidth("Learn") + 10);
                learnButton.setEnabled(canLearn);
                learnButton.setPressedCallback((button, buttonType) -> {
                    PacketHandler.sendMessageToServer(new PlayerLearnAbilityRequestPacket(getAbility().getAbilityId(), trainerEntityId));
                    return true;
                });
                nameTray.addWidget(learnButton);
            }
        } else {
            MKText prompt = new MKText(font, "Select an ability to learn.");
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
