package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerLearnAbilityRequestPacket;
import com.chaosbuffalo.mkwidgets.client.gui.instructions.HoveringTextInstruction;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.math.Vec2i;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LearnAbilityTray extends MKStackLayoutHorizontal {
    private MKAbility ability;
    private List<ITextComponent> unmetRequirements;
    private final MKPlayerData playerData;
    private final FontRenderer font;
    private final int trainerEntityId;

    public LearnAbilityTray(int x, int y, int height, MKPlayerData playerData, FontRenderer font, int trainerEntityId) {
        super(x, y, height);
        this.playerData = playerData;
        this.trainerEntityId = trainerEntityId;
        unmetRequirements = new ArrayList<>();
        this.font = font;
        this.ability = null;
        setPaddingLeft(2);
        setPaddingRight(2);
        setup();
    }

    public void setup() {
        clearWidgets();
        if (getAbility() != null) {
            MKText abilityName = new MKText(font, getAbility().getAbilityName());
            abilityName.setWidth(font.getStringWidth(getAbility().getAbilityName()));
            addWidget(abilityName);

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

            MKButton learnButton = new MKButton(0, 0, "Learn") {

                @Override
                public boolean checkHovered(int mouseX, int mouseY) {
                    return this.isVisible() && this.isInBounds(mouseX, mouseY);
                }

                @Override
                public void onMouseHover(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
                    super.onMouseHover(mc, mouseX, mouseY, partialTicks);
                    if (!isKnown && unmetRequirements.size() > 0) {
                        ArrayList<String> texts = unmetRequirements.stream()
                                .map(ITextComponent::getFormattedText)
                                .collect(Collectors.toCollection(ArrayList::new));
                        if (getScreen() != null) {
                            getScreen().addPostRenderInstruction(new HoveringTextInstruction(texts, getParentCoords(new Vec2i(mouseX, mouseY))));
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
            addWidget(learnButton);
        } else {
            MKText prompt = new MKText(font, "Select an ability to learn.");
            addWidget(prompt);
        }
    }

    public void setAbility(MKAbility ability, List<ITextComponent> requirements) {
        this.ability = ability;
        this.unmetRequirements = requirements;
        setup();
    }

    public MKAbility getAbility() {
        return ability;
    }
}
