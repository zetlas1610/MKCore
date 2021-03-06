package com.chaosbuffalo.mkcore.core.talents;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public class TalentRecord {

    private int currentRank;
    private final TalentNode node;

    public TalentRecord(TalentNode node) {
        this.node = node;
        currentRank = 0;
    }

    public TalentNode getNode() {
        return node;
    }

    public boolean isKnown() {
        return currentRank > 0;
    }

    public int getRank() {
        return currentRank;
    }

    public boolean modifyRank(int value) {
        int next = currentRank + value;
        if (next >= 0 && next <= node.getMaxRanks()) {
            setRank(next);
            return true;
        }
        return false;
    }

    public void setRank(int value) {
        currentRank = value;
    }

    public <T> T serialize(DynamicOps<T> ops) {
        ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
        builder.put(ops.createString("rank"), ops.createInt(currentRank));
        return ops.createMap(builder.build());
    }

    public <T> void deserialize(Dynamic<T> dynamic) {
        currentRank = dynamic.get("rank").asInt(0);
    }

    public String toString() {
        return String.format("TalentRecord{node=%s, rank=%d}", node, currentRank);
    }
}
