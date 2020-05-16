package com.chaosbuffalo.mkcore.old;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.core.IPlayerAbilityInfo;
import com.chaosbuffalo.mkcore.core.IPlayerClassInfo;
import com.chaosbuffalo.mkcore.old.PlayerClass;
import com.chaosbuffalo.mkcore.sync.CompositeUpdater;
import com.chaosbuffalo.mkcore.sync.SyncInt;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class PlayerClassInfo implements IPlayerClassInfo {
    private final PlayerClass playerClass;
    private ResourceLocation classId;
    private final SyncInt level = new SyncInt("level", 1);
    private Map<ResourceLocation, IPlayerAbilityInfo> abilityInfoMap = new HashMap<>(GameConstants.ACTION_BAR_SIZE);
    private CompositeUpdater dirtyUpdater = new CompositeUpdater(level);

    public PlayerClassInfo(PlayerClass playerClass) {
        this.playerClass = playerClass;
        this.classId = playerClass.getClassId();
    }

    public PlayerClass getClassDefinition() {
        return playerClass;
    }
}
