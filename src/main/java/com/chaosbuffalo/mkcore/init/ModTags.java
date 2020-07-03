package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;

public class ModTags {
    public static class Items {
        public static final Tag<Item> LIGHT_ARMOR = tag("armor/light");
        public static final Tag<Item> MEDIUM_ARMOR = tag("armor/medium");
        public static final Tag<Item> HEAVY_ARMOR = tag("armor/heavy");

        private static Tag<Item> tag(String name) {
            return new ItemTags.Wrapper(MKCore.makeRL(name));
        }

    }
}
