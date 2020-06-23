package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.core.MKPlayerData;

import java.util.function.Function;

public class TalentType<T extends TalentTypeHandler> {
    public static TalentType<AttributeTalentHandler> ATTRIBUTE = new TalentType<>("Attribute", AttributeTalentHandler::new);
    public static TalentType<PassiveTalentHandler> PASSIVE = new TalentType<>("Passive", PassiveTalentHandler::new);
    public static TalentType<UltimateTalentHandler> ULTIMATE = new TalentType<>("Ultimate", UltimateTalentHandler::new);

    private final String name;
    private final Function<MKPlayerData, T> factory;

    private TalentType(String name, Function<MKPlayerData, T> factory) {
        this.name = name;
        this.factory = factory;
    }

    public String getName() {
        return name;
    }

    public T createTypeHandler(MKPlayerData playerData) {
        return factory.apply(playerData);
    }
}
