package com.chaosbuffalo.mkcore.abilities.training;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class HeldItemRequirement implements IAbilityLearnRequirement {
    private final Item item;
    private final Hand hand;

    public HeldItemRequirement(Item item, Hand hand) {
        this.item = item;
        this.hand = hand;
    }

    @Override
    public boolean check(IMKEntityData entityData, MKAbility ability) {
        ItemStack stack = entityData.getEntity().getHeldItem(hand);
        if (stack.isEmpty())
            return false;

        return stack.getItem() == item;
    }

    @Override
    public void onLearned(IMKEntityData entityData, MKAbility ability) {

    }

    @Override
    public ITextComponent describe() {
        String handName = hand == Hand.MAIN_HAND ? "Main" : "Off";
        return new StringTextComponent("You must be holding a ")
                .appendSibling(item.getName())
                .appendSibling(new StringTextComponent(String.format(" in your %s hand", handName)));
    }
}
