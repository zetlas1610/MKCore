package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.sync.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.util.*;
import java.util.function.BiConsumer;

public class AbilityTracker implements ISyncObject {

    private int ticks;
    private final Map<ResourceLocation, TimerEntry> timers = new HashMap<>();

    public boolean hasCooldown(ResourceLocation id) {
        return getCooldownTicks(id) > 0;
    }

    public float getCooldownPercent(ResourceLocation id, float partialTicks) {
        TimerEntry cd = timers.get(id);

        if (cd != null) {
            float totalCooldown = cd.getMaxTicks();
            float currentCooldown = (float) cd.expireTicks - ((float) this.ticks + partialTicks);
            return MathHelper.clamp(currentCooldown / totalCooldown, 0.0F, 1.0F);
        } else {
            return 0.0F;
        }
    }

    public int getCooldownTicks(ResourceLocation id) {
        TimerEntry cd = timers.get(id);
        return cd != null ? cd.getRemainingTicks(ticks) : 0;
    }

    public int getMaxCooldownTicks(ResourceLocation id) {
        TimerEntry cd = timers.get(id);
        return cd != null ? cd.getMaxTicks() : 0;
    }

    public void tick() {
        ticks++;

        if (timers.isEmpty())
            return;

        Iterator<Map.Entry<ResourceLocation, TimerEntry>> iterator = timers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ResourceLocation, TimerEntry> entry = iterator.next();

            if (entry.getValue().isExpired(ticks)) {
                iterator.remove();
                onTimerRemoved(entry.getKey());
            }
        }
    }

    public void setCooldown(ResourceLocation id, int ticksIn) {
        setCooldownInternal(id, ticksIn);
        onTimerAdded(id, ticksIn);
    }

    protected void setCooldownInternal(ResourceLocation id, int timerTicks) {
        timers.put(id, new TimerEntry(ticks, ticks + timerTicks));
    }

    public void removeCooldown(ResourceLocation id) {
        timers.remove(id);
        onTimerRemoved(id);
    }

    protected void onTimerAdded(ResourceLocation id, int ticksIn) {
    }

    protected void onTimerRemoved(ResourceLocation id) {
    }

    public void serialize(CompoundNBT nbt) {
        CompoundNBT root = new CompoundNBT();
        iterateActive((id, cd) -> root.putInt(id.toString(), cd));
        nbt.put("cooldowns", root);
    }

    public void deserialize(CompoundNBT nbt) {
        if (nbt.contains("cooldowns")) {
            CompoundNBT root = nbt.getCompound("cooldowns");
            for (String key : root.keySet()) {
                setCooldownInternal(new ResourceLocation(key), root.getInt(key));
            }
        }
    }

    void iterateActive(BiConsumer<ResourceLocation, Integer> consumer) {
        for (ResourceLocation id : timers.keySet()) {
            int cd = getCooldownTicks(id);
            if (cd > 0) {
                consumer.accept(id, cd);
            }
        }
    }

    void removeAll() {
        Iterator<Map.Entry<ResourceLocation, TimerEntry>> iterator = timers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ResourceLocation, TimerEntry> entry = iterator.next();
            iterator.remove();
            onTimerRemoved(entry.getKey());
        }
    }

    static class TimerEntry {
        final int createTicks;
        final int expireTicks;

        private TimerEntry(int startTime, int expiration) {
            createTicks = startTime;
            expireTicks = expiration;
        }

        public int getRemainingTicks(int currentTicks) {
            return Math.max(0, expireTicks - currentTicks);
        }

        public int getMaxTicks() {
            return Math.max(0, expireTicks - createTicks);
        }

        public boolean isExpired(int currentTicks) {
            return expireTicks <= currentTicks;
        }
    }

    static class AbilityTrackerServer extends AbilityTracker {

        private final List<ResourceLocation> dirty = new ArrayList<>();
        private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

        public AbilityTrackerServer() {
        }

        @Override
        protected void onTimerAdded(ResourceLocation id, int ticksIn) {
            super.onTimerAdded(id, ticksIn);
            dirty.add(id);
            parentNotifier.notifyUpdate(this);
        }

        @Override
        protected void onTimerRemoved(ResourceLocation id) {
            super.onTimerRemoved(id);
            dirty.add(id);
            parentNotifier.notifyUpdate(this);
        }

        @Override
        public void setNotifier(ISyncNotifier notifier) {
            parentNotifier = notifier;
        }

        @Override
        public boolean isDirty() {
            return dirty.size() > 0;
        }

        @Override
        public void serializeUpdate(CompoundNBT tag) {
            CompoundNBT root = new CompoundNBT();
            dirty.forEach(id -> root.putInt(id.toString(), getCooldownTicks(id)));
            tag.put("cooldowns", root);
            dirty.clear();
        }

        @Override
        public void serializeFull(CompoundNBT tag) {
            serialize(tag);
            dirty.clear();
        }
    }

    public static AbilityTracker getTracker(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            return new AbilityTrackerServer();
        } else {
            return new AbilityTracker();
        }
    }

    @Override
    public void setNotifier(ISyncNotifier notifier) {

    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public void deserializeUpdate(CompoundNBT tag) {
        deserialize(tag);
    }

    @Override
    public void serializeUpdate(CompoundNBT tag) {

    }

    @Override
    public void serializeFull(CompoundNBT tag) {

    }
}
