package com.chaosbuffalo.mkcore.mku.entity.ai.memory;

public class ThreatMapEntry {
    private int currentThreat;

    public ThreatMapEntry() {
        currentThreat = 0;
    }

    public int getCurrentThreat() {
        return currentThreat;
    }

    public ThreatMapEntry addThreat(int value) {
        currentThreat += value;
        return this;
    }

    public ThreatMapEntry subtractThreat(int value) {
        currentThreat -= value;
        return this;
    }
}
