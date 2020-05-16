package com.chaosbuffalo.mkcore.old;

import com.chaosbuffalo.mkcore.core.IPlayerAbility;
import com.chaosbuffalo.mkcore.core.IPlayerClass;
import com.chaosbuffalo.mkcore.core.IPlayerClassInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.List;

public abstract class PlayerClass extends ForgeRegistryEntry<PlayerClass> implements IPlayerClass {

    private final ResourceLocation classId;

    protected PlayerClass(String modId, String pathName) {
        this(new ResourceLocation(modId, pathName));
    }

    public PlayerClass(ResourceLocation classId) {
        this.classId = classId;
    }

    @Override
    public ResourceLocation getClassId() {
        return classId;
    }

    public IPlayerClassInfo createClassInfo() {
        return new PlayerClassInfo(this);
    }

    public String getClassName() {
        return I18n.format(getTranslationKey());
    }

    public String getTranslationKey() {
        return String.format("%s.%s.name", classId.getNamespace(), classId.getPath());
    }

    public abstract int getBaseHealth();

    public abstract int getHealthPerLevel();

    public abstract float getBaseManaRegen();

    public abstract float getManaRegenPerLevel();

    public abstract int getBaseMana();

    public String hashAbilities() {
        StringBuilder concatIds = new StringBuilder();
        for (IPlayerAbility ability : getAbilities()){
            concatIds.append(ability.getAbilityId().toString());
        }
        return DigestUtils.sha1Hex(concatIds.toString());
    }

    public abstract int getManaPerLevel();

    public abstract IClassClientData getClientData();

    public abstract ArmorClass getArmorClass();

    public IPlayerAbility getOfferedAbilityBySlot(int slotIndex) {
        List<IPlayerAbility> abilities = getAbilities();
        if (slotIndex < abilities.size()) {
            return abilities.get(slotIndex);
        }
        return null;
    }

    protected abstract List<IPlayerAbility> getAbilities();
}