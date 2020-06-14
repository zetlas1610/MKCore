package com.chaosbuffalo.mkcore.abilities.attributes;


public abstract class AbilityAttribute<T> implements IAbilityAttribute<T> {
    private final T defaultValue;
    private T currentValue;
    private final String name;

    public AbilityAttribute(String name, T defaultValue) {
        this.defaultValue = defaultValue;
        currentValue = defaultValue;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public T getValue() {
        return currentValue;
    }

    @Override
    public void setValue(T newValue) {
        this.currentValue = newValue;
    }
}
