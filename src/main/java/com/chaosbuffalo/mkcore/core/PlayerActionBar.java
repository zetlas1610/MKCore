package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.PlayerAbility;
import com.chaosbuffalo.mkcore.abilities.PlayerAbilityInfo;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.function.Supplier;

public class PlayerActionBar implements ISyncObject {

    private final PlayerKnowledge knowledge;
    private final List<ResourceLocation> hotbar = NonNullList.withSize(GameConstants.ACTION_BAR_SIZE, MKCoreRegistry.INVALID_ABILITY);
    private final HotBarUpdater hotBarUpdater = new HotBarUpdater(this);


    public PlayerActionBar(PlayerKnowledge knowledge) {
        this.knowledge = knowledge;
    }

    public int getCurrentSize() {
        // TODO: expandable
        return GameConstants.CLASS_ACTION_BAR_SIZE;
    }


    public int getSlotForAbility(ResourceLocation abilityId) {
        int slot = hotbar.indexOf(abilityId);
        if (slot == -1)
            return GameConstants.ACTION_BAR_INVALID_SLOT;
        return slot;
    }

    public ResourceLocation getAbilityInSlot(int slot) {
        if (slot < hotbar.size()) {
            return hotbar.get(slot);
        }
        return MKCoreRegistry.INVALID_ABILITY;
    }

    public void setAbilityInSlot(int index, ResourceLocation abilityId) {
        if (index < hotbar.size()) {
            hotbar.set(index, abilityId);
            hotBarUpdater.setDirty(index);
        }
    }

    private void removeFromHotBar(ResourceLocation abilityId) {
        int slot = getSlotForAbility(abilityId);
        if (slot != GameConstants.ACTION_BAR_INVALID_SLOT) {
            setAbilityInSlot(slot, MKCoreRegistry.INVALID_ABILITY);
        }
    }

    private int getFirstFreeAbilitySlot() {
        return getSlotForAbility(MKCoreRegistry.INVALID_ABILITY);
    }

    public int tryPlaceOnBar(ResourceLocation abilityId) {
        int slot = getSlotForAbility(abilityId);
        if (slot == GameConstants.ACTION_BAR_INVALID_SLOT) {
            // Skill was just learned so let's try to put it on the bar
            slot = getFirstFreeAbilitySlot();
            if (slot != GameConstants.ACTION_BAR_INVALID_SLOT) {
                setAbilityInSlot(slot, abilityId);
            }
        }

        return slot;
    }

    public void onAbilityUnlearned(PlayerAbility ability) {
        removeFromHotBar(ability.getAbilityId());
    }

    private void checkHotBar(ResourceLocation abilityId) {
        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY))
            return;
        PlayerAbilityInfo info = knowledge.getAbilityInfo(abilityId);
        if (info == null)
            return;
        if (!info.isCurrentlyKnown()) {
            removeFromHotBar(info.getId());
        }
    }

    @Override
    public boolean isDirty() {
        return hotBarUpdater.isDirty();
    }

    @Override
    public void deserializeUpdate(CompoundNBT tag) {
        hotBarUpdater.deserializeUpdate(tag);
    }

    @Override
    public void serializeUpdate(CompoundNBT tag) {
        hotBarUpdater.serializeUpdate(tag);
    }

    @Override
    public void serializeFull(CompoundNBT tag) {
        hotBarUpdater.serializeFull(tag);
    }


    static class HotBarUpdater extends ListUpdater {
        public HotBarUpdater(PlayerActionBar actionBar) {
            super(() -> actionBar.hotbar, "hotbar");
        }
    }

    abstract static class ListUpdater implements ISyncObject {
        private final Supplier<List<ResourceLocation>> parent;
        private final String name;
        private final IntSet dirtyEntries = new IntOpenHashSet();

        public ListUpdater(Supplier<List<ResourceLocation>> list, String name) {
            this.parent = list;
            this.name = name;
        }

        private CompoundNBT makeEntry(int index, ResourceLocation value) {
            CompoundNBT tag = new CompoundNBT();
            tag.putInt("i", index);
            tag.putString("v", value.toString());
            return tag;
        }

        void setDirty(int index) {
            dirtyEntries.add(index);
        }

        @Override
        public boolean isDirty() {
            return dirtyEntries.size() > 0;
        }

        @Override
        public void deserializeUpdate(CompoundNBT tag) {
            ListNBT list = tag.getList(name, Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < list.size(); i++) {
                CompoundNBT entry = list.getCompound(i);
                int index = entry.getInt("i");
                ResourceLocation value = new ResourceLocation(entry.getString("v"));
                List<ResourceLocation> abilityList = parent.get();
                if (abilityList != null) {
                    abilityList.set(index, value);
                }
            }
        }

        @Override
        public void serializeUpdate(CompoundNBT tag) {
            if (dirtyEntries.size() > 0) {
                List<ResourceLocation> fullList = parent.get();
                ListNBT list = tag.getList(name, Constants.NBT.TAG_COMPOUND);
                dirtyEntries.forEach((int i) -> {
                    list.add(list.size(), makeEntry(i, fullList.get(i)));
                });
                tag.put(name, list);
                dirtyEntries.clear();
            }
        }

        @Override
        public void serializeFull(CompoundNBT tag) {
            List<ResourceLocation> fullList = parent.get();
            ListNBT list = tag.getList(name, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < fullList.size(); i++) {
                list.add(i, makeEntry(i, fullList.get(i)));
            }
            tag.put(name, list);
        }
    }
}
