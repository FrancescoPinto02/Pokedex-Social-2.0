package com.pokedexsocial.backend.util;

/**
 * Generic class for Numeric Range
 * @param <T> - Numeric Type (eg. Integer)
 */
public class Range<T extends Number> {
    private T min;
    private T max;

    //Constructors
    public Range() {
    }
    public Range(T min, T max) {
        this.min = min;
        this.max = max;
    }

    //Getters & Setters
    public T getMin() {
        return min;
    }

    public void setMin(T min) {
        this.min = min;
    }

    public T getMax() {
        return max;
    }

    public void setMax(T max) {
        this.max = max;
    }
}
