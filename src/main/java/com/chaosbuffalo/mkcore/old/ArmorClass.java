package com.chaosbuffalo.mkcore.old;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ArmorClass {
    public static final ArmorClass ROBES = new ArmorClass(MKCore.makeRL("armor_class.robes"));
    public static final ArmorClass LIGHT = new ArmorClass(new ResourceLocation(MKCore.MOD_ID, "armor_class.light")).setParent(ROBES);
    public static final ArmorClass MEDIUM = new ArmorClass(new ResourceLocation(MKCore.MOD_ID, "armor_class.medium")).setParent(LIGHT);
    public static final ArmorClass HEAVY = new ArmorClass(new ResourceLocation(MKCore.MOD_ID, "armor_class.heavy")).setParent(MEDIUM);
    public static final ArmorClass ALL = new AllowAllArmorClass(new ResourceLocation(MKCore.MOD_ID, "armor_class.all")).setParent(HEAVY);
    private static final ArmorClass NOT_CLASSIFIED = new ArmorClass(new ResourceLocation(MKCore.MOD_ID, "armor_class.undefined"));
    private static final List<ArmorClass> CHECK_ORDER = Arrays.asList(ROBES, LIGHT, MEDIUM, HEAVY);

    private final ResourceLocation location;
    private final Set<IArmorMaterial> materials = new HashSet<>();
    private ArmorClass parent;
    private ArmorClass next;
    private final Set<ArmorItem> itemOverrides = new HashSet<>();

    public static void clearArmorClasses() {
        ROBES.clear();
        LIGHT.clear();
        MEDIUM.clear();
        HEAVY.clear();
    }

    private static ArmorClass getArmorClassForMaterial(IArmorMaterial material) {
        for (ArmorClass armorClass : CHECK_ORDER) {
            if (armorClass.hasMaterial(material))
                return armorClass;
        }
        return NOT_CLASSIFIED;
    }

    public static ArmorClass getArmorClassForArmorItem(ArmorItem item) {
        for (ArmorClass armorClass : CHECK_ORDER) {
            if (armorClass.hasItemOverride(item)) {
                return armorClass;
            }
        }
        return getArmorClassForMaterial(item.getArmorMaterial());
    }

    public ArmorClass(ResourceLocation location) {
        this.location = location;
    }

    private void clear() {
        materials.clear();
        itemOverrides.clear();
    }

    public String getName() {
        return I18n.format(String.format("%s.%s.name", location.getNamespace(), location.getPath()));
    }

    public ResourceLocation getLocation() {
        return location;
    }

    private boolean hasMaterial(IArmorMaterial material) {
        return materials.contains(material);
    }

    private boolean canWearMaterial(IArmorMaterial material) {
        return hasMaterial(material) || (parent != null && parent.canWearMaterial(material));
    }

    public ArmorClass getSuccessor() {
        if (next != null)
            return next;
        return this;
    }

    public boolean canWear(ArmorItem item) {
        return canWearItem(item) || canWearMaterial(item.getArmorMaterial());
    }

    private boolean hasItemOverride(ArmorItem item) {
        return itemOverrides.contains(item);
    }

    private boolean canWearItem(ArmorItem item) {
        return hasItemOverride(item) || (parent != null && parent.canWearItem(item));
    }

    ArmorClass setParent(ArmorClass armorClass) {
        armorClass.next = this;
        parent = armorClass;
        return this;
    }

    public ArmorClass registerItem(ArmorItem item) {
        itemOverrides.add(item);
        return this;
    }

    public ArmorClass register(IArmorMaterial material) {
        materials.add(material);
        return this;
    }

    private static class AllowAllArmorClass extends ArmorClass {
        AllowAllArmorClass(ResourceLocation location) {
            super(location);
        }

        @Override
        public boolean canWear(ArmorItem armor) {
            return true;
        }
    }
}
