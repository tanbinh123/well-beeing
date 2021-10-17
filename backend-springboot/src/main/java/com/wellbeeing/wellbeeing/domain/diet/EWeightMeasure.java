package com.wellbeeing.wellbeeing.domain.diet;

public enum EWeightMeasure {
    MICRO_GRAM(0.000001),
    MILLI_GRAM(0.001),
    GRAM(1),
    KILO_GRAM(1000);

    private double numberOfGram;

    EWeightMeasure(double numberOfGram) {
        this.numberOfGram = numberOfGram;
    }

    public double getNumberOfGram() {
        return numberOfGram;
    }
}
